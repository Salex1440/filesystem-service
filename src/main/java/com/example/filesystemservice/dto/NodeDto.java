package com.example.filesystemservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class NodeDto {

    private String type;

    private String url;

    private String id;

    private String parentId;

    private int size;

    private List<NodeDto> children;

    private String updateDate;

}
