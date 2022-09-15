package com.example.filesystemservice.service;

import com.example.filesystemservice.dto.*;
import com.example.filesystemservice.exception.BadRequestException;
import com.example.filesystemservice.exception.NotFoundException;
import com.example.filesystemservice.repository.Node;
import com.example.filesystemservice.repository.NodeRepository;
import com.example.filesystemservice.repository.Record;
import com.example.filesystemservice.repository.RecordRepository;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class NodeService {

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private RecordRepository recordRepository;

    public void importBatch(String batch) {
        Gson gson = new Gson();
        BatchDto batchDto = gson.fromJson(batch, BatchDto.class);
        Set<String> ids = new HashSet<>();
        Date date;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(batchDto.getUpdateDate());
        } catch (ParseException e) {
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

    private void updateDate(Node node, Date date) {
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String dateStr = dateFormat.format(node.getDate());
        nodeDto.setDate(dateStr);
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

    public List<NodeDto> findUpdatedNodes(String dateStr) {
        Date date;

        try {
            date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(dateStr);
        } catch (ParseException e) {
            throw new BadRequestException("Validation Failed");
        }
        List<NodeDto> nodeDtos = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, -24);
        Date lowDate = calendar.getTime();
        List<Node> nodes = nodeRepository.findByDateBetween(lowDate, date);
        for (Node node : nodes) {
            NodeDto nodeDto = new NodeDto();
            nodeDto.setId(node.getId());
            nodeDto.setType(node.getType());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            nodeDto.setDate(dateFormat.format(node.getDate()));
            nodeDto.setSize(node.getSize());
            nodeDto.setUrl(node.getUrl());
            nodeDto.setParentId(node.getParentId());
            nodeDtos.add(nodeDto);
        }
        return nodeDtos;
    }

    public HistoryDto getHistory(String id, String from, String to) {
        HistoryDto historyDto = new HistoryDto();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date dateFrom, dateTo;
        try {
            dateFrom = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(from);
            dateTo = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(to);
        } catch (ParseException e) {
            throw new BadRequestException("Validation Failed");
        }
        List<Record> records = recordRepository.findByNodeIdBetweenDate(id, dateFrom, dateTo);
        List<RecordDto> recordDtoList = new ArrayList<>();
        if (records == null) {
            throw new NotFoundException("Item not found");
        }
        for (Record record : records) {
            RecordDto recordDto = new RecordDto();
            recordDto.setId(record.getNodeId());
            recordDto.setParentId(recordDto.getParentId());
            recordDto.setUrl(record.getUrl());
            recordDto.setType(record.getType());
            recordDto.setSize(record.getSize());
            String dateStr = dateFormat.format(record.getUpdateDate());
            recordDto.setDate(dateStr);
            recordDtoList.add(recordDto);
        }
        historyDto.setItems(recordDtoList);
        return historyDto;
    }

}
