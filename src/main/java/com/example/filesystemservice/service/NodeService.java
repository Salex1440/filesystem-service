package com.example.filesystemservice.service;

import com.example.filesystemservice.dto.BatchDto;
import com.example.filesystemservice.dto.ItemDto;
import com.example.filesystemservice.exception.BadRequestException;
import com.example.filesystemservice.exception.UnprocessableEntityException;
import com.example.filesystemservice.repository.Node;
import com.example.filesystemservice.repository.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;


public class NodeService {

    @Autowired
    private NodeRepository nodeRepository;

    public void importNode(BatchDto batch) {
        String updateDate = batch.getUpdateDate();
        for (ItemDto item : batch.getItems()) {
            Node node = nodeRepository.findNodeById(item.getId());
            Node newParentNode = nodeRepository.findNodeById(item.getParentId());
            if (newParentNode.getType().equals(NodeType.FILE.toString())) {
                throw new UnprocessableEntityException("Item of type \"FILE\" can't be parent!");
            }
            if (!node.getType().equals(item.getType())) {
                throw new UnprocessableEntityException("Can't change type of an item!");
            }
            if (item.getType().equals(NodeType.FOLDER.toString())) {
                if (item.getUrl() != null) {
                    throw new UnprocessableEntityException("For a node of type \"FOLDER\" URL must be NULL!");
                }
                if (item.getSize() != 0) {
                    throw new BadRequestException("FOLDER size must be NULL!");
                }
            } else if (item.getType().equals((NodeType.FILE).toString())) {
                if (item.getUrl().length() > 255) {
                    throw new BadRequestException("URL size must be less or equal than 255!");
                }
                if (item.getType().equals((NodeType.FILE).toString()) && item.getUrl().length() == 0) {
                    throw new BadRequestException("URL size must be greater than 0!");
                }
            }
            if (node == null) {
                node.setId(item.getId());
                node.setType(item.getType());
                node.setUrl(item.getUrl());
                node.setDate(updateDate);
                node.setSize(item.getSize());
                node.setParentId(item.getParentId());
                nodeRepository.save(node);
            } else {
                node.setUrl(item.getUrl());
                node.setDate(updateDate);
                node.setSize(item.getSize());
                node.setParentId(item.getParentId());
                nodeRepository.save(node);
            }
        }
    }


}
