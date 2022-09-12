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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NodeService {

    @Autowired
    private NodeRepository nodeRepository;

    public void importBatch(String batch) {
        Gson gson = new Gson();
        BatchDto batchDto = gson.fromJson(batch, BatchDto.class);
        Set<String> ids = new HashSet<>();
        String updateDate = batchDto.getUpdateDate();
        for (ItemDto item : batchDto.getItems()) {
            Node node = nodeRepository.findNodeById(item.getId());
            Node newParentNode = nodeRepository.findNodeById(item.getParentId());
            String regex = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(updateDate);
            if (!matcher.matches()) {
                throw new BadRequestException("Validation Failed");
            }
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
            node.setDate(updateDate);
            node.setSize(item.getSize());
            node.setParentId(item.getParentId());
            nodeRepository.save(node);
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
        nodeDto.setDate(node.getDate());
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

}
