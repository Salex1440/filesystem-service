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
            if (node == null) {
                if (item.getType().equals(NodeType.FOLDER.toString()) && item.getUrl() != null) {
                    throw new UnprocessableEntityException("For a node of type \"FOLDER\" URL must be NULL!");
                } else if (item.getType().equals((NodeType.FILE).toString()) && item.getUrl().length() > 255) {
                    throw new BadRequestException("URL size must be less or equal than 255!");
                }

                node.setId(item.getId());
                node.setType(item.getType());
                node.setUrl(item.getUrl());
                node.setDate(updateDate);
                node.setSize(item.getSize());
                node.setParentId(item.getParentId());
                nodeRepository.save(node);
            } else {
                Node newParentNode = nodeRepository.findNodeById(item.getParentId());
                if (newParentNode.getType().equals(NodeType.FILE.toString())) {
                    throw new UnprocessableEntityException("Item of type \"FILE\" can't be parent!");
                }
                if (!node.getType().equals(item.getType())) {
                    throw new UnprocessableEntityException("Can't change type of the item!");
                }
                if (item.getType().equals(NodeType.FOLDER.toString()) && item.getUrl() != null) {
                    throw new UnprocessableEntityException("For a node of type \"FOLDER\" URL must be NULL!");
                } else if (item.getType().equals((NodeType.FILE).toString()) && item.getUrl().length() > 255) {
                    throw new BadRequestException("URL size must be less or equal than 255!");
                }
                node.setUrl(item.getUrl());
                node.setDate(updateDate);
                node.setSize(item.getSize());
                node.setParentId(item.getParentId());
                nodeRepository.save(node);
            }
        }
    }


}
