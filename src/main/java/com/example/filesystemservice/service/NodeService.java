package com.example.filesystemservice.service;

import com.example.filesystemservice.dto.BatchDto;
import com.example.filesystemservice.dto.ItemDto;
import com.example.filesystemservice.exception.BadRequestException;
import com.example.filesystemservice.exception.UnprocessableEntityException;
import com.example.filesystemservice.repository.Node;
import com.example.filesystemservice.repository.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NodeService {

    @Autowired
    private NodeRepository nodeRepository;

    public void importNode(BatchDto batch) {
        Set<String> ids = new HashSet<>();
        String updateDate = batch.getUpdateDate();
        for (ItemDto item : batch.getItems()) {
            Node node = nodeRepository.findNodeById(item.getId());
            Node newParentNode = nodeRepository.findNodeById(item.getParentId());
            String regex = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(updateDate);
            if (!matcher.matches()) {
                throw new BadRequestException("Bad date format!");
            }
            if (ids.contains(item.getId())) {
                throw new BadRequestException("There are two or more same ids in the request!");
            } else {
                ids.add(item.getId());
            }
            if (newParentNode != null && newParentNode.getType().equals(NodeType.FILE.toString())) {
                throw new UnprocessableEntityException("Item of type \"FILE\" can't be parent!");
            }
            if (node != null && !node.getType().equals(item.getType())) {
                throw new UnprocessableEntityException("Can't change type of an item!");
            }
            if (item.getType().equals(NodeType.FOLDER.toString())) {
                if (item.getUrl() != null) {
                    throw new BadRequestException("For a node of type \"FOLDER\" URL must be NULL!");
                }
                if (item.getSize() != 0) {
                    throw new BadRequestException("FOLDER size must be NULL!");
                }
            } else if (item.getType().equals((NodeType.FILE).toString())) {
                if (item.getUrl().length() > 255) {
                    throw new BadRequestException("URL size must be less or equal than 255!");
                }
                if (item.getSize() == 0) {
                    throw new BadRequestException("FILE size must be not NULL!");
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


}
