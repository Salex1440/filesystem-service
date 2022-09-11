package com.example.filesystemservice.service;

import com.example.filesystemservice.dto.BatchDto;
import com.example.filesystemservice.dto.ItemDto;
import com.example.filesystemservice.exception.BadParentException;
import com.example.filesystemservice.repository.Node;
import com.example.filesystemservice.repository.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


public class NodeService {

    @Autowired
    private NodeRepository nodeRepository;

    public void importNode(BatchDto batch) {
        String updateDate = batch.getUpdateDate();
        for (ItemDto item : batch.getItems()) {
            Node node = nodeRepository.findNodeById(item.getId());
            if (node == null) {
                node.setId(item.getId());
                node.setType(item.getType());
                node.setUrl(item.getUrl());
                node.setDate(updateDate);
                node.setSize(item.getSize());
                node.setParentId(item.getParentId());
                nodeRepository.save(node);
            } else {
                Node newParentNode = nodeRepository.findNodeById(item.getParentId());
                if (newParentNode.getType().equals("FILE")) {
                    throw new BadParentException("Item of type \"FILE\" can't be parent!");
                }
            }
        }
    }



}
