package com.example.filesystemservice.service;

import com.example.filesystemservice.dto.BatchDto;
import com.example.filesystemservice.dto.ItemDto;
import com.example.filesystemservice.dto.NodeDto;
import com.example.filesystemservice.exception.BadRequestException;
import com.example.filesystemservice.exception.NotFoundException;
import com.example.filesystemservice.repository.Node;
import com.example.filesystemservice.repository.NodeRepository;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class NodeService {

    @Autowired
    private NodeRepository nodeRepository;

    public void importBatch(String batch) {
        Gson gson = new Gson();
        BatchDto batchDto = gson.fromJson(batch, BatchDto.class);
        Set<String> ids = new HashSet<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        LocalDateTime date;
        try {
            date = LocalDateTime.parse(batchDto.getUpdateDate(), formatter);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Validation Failed");
        }
        for (ItemDto item : batchDto.getItems()) {
            Node node = nodeRepository.findNodeById(item.getId());
            Node newParentNode = nodeRepository.findNodeById(item.getParentId());
            if (ids.contains(item.getId())) {
                throw new BadRequestException("Validation Failed");
            } else {
                ids.add(item.getId());
            }
            if (newParentNode != null && newParentNode.getType().equals(NodeType.FILE.toString())) {
                throw new BadRequestException("Validation Failed");
            }
            if (node != null && !node.getType().equals(item.getType())) {
                throw new BadRequestException("Validation Failed");
            }
            if (item.getType().equals(NodeType.FOLDER.toString())) {
                if (item.getUrl() != null) {
                    throw new BadRequestException("Validation Failed");
                }
                if (item.getSize() != 0) {
                    throw new BadRequestException("Validation Failed");
                }
            } else if (item.getType().equals((NodeType.FILE).toString())) {
                if (item.getUrl().length() > 255) {
                    throw new BadRequestException("Validation Failed");
                }
                if (item.getSize() == 0) {
                    throw new BadRequestException("Validation Failed");
                }
            }
            if (node == null) {
                node = new Node();
                node.setId(item.getId());
                node.setType(item.getType());
            }
            node.setUrl(item.getUrl());
            node.setDate(date);
            node.setSize(item.getSize());
            node.setParentId(item.getParentId());
            nodeRepository.save(node);
            if (newParentNode != null) {
                updateDate(newParentNode, date);
            }
        }
    }

    private void updateDate(Node node, LocalDateTime date) {
        node.setDate(date);
        nodeRepository.save(node);
        Node parentNode = nodeRepository.findNodeById(node.getParentId());
        if (parentNode != null) {
            updateDate(parentNode, date);
        }
    }

    public NodeDto getNodeById(String id) {
        Node node = nodeRepository.findNodeById(id);
        if (node == null) {
            throw new NotFoundException("Item not found");
        }
        List<Node> children = nodeRepository.findNodesByParentId(id);
        NodeDto nodeDto = new NodeDto();
        nodeDto.setId(node.getId());
        nodeDto.setType(node.getType());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        nodeDto.setDate(node.getDate().format(formatter));
        nodeDto.setSize(node.getSize());
        nodeDto.setUrl(node.getUrl());
        nodeDto.setParentId(node.getParentId());
        if (node.getType().equals(NodeType.FOLDER.toString())) {
            List<NodeDto> childrenDto = new ArrayList<>();
            int size = 0;
            if (children != null) {
                for (Node child : children) {
                    NodeDto childDto = getNodeById(child.getId());
                    childrenDto.add(childDto);
                    size += childDto.getSize();
                }
            }
            nodeDto.setChildren(childrenDto);
            nodeDto.setSize(size);
        } else if (node.getType().equals(NodeType.FILE.toString())) {
            nodeDto.setChildren(null);
        }
        return nodeDto;
    }

    public void deleteNodeById(String id) {
        Node node = nodeRepository.findNodeById(id);
        if (node == null) {
            throw new NotFoundException("Item not found");
        }
        List<Node> children = nodeRepository.findNodesByParentId(id);
        nodeRepository.delete(node);
        if (children != null) {
            for (Node child : children) {
                deleteNodeById(child.getId());
            }
        }
    }

    public List<Node> findUpdatedNodes(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        LocalDateTime date;
        try {
            date = LocalDateTime.parse(dateStr, formatter);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Validation Failed");
        }
        return nodeRepository.findUpdatedNodes(date.minusHours(24), date);
    }

}
