package com.example.filesystemservice.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RecordDto {

    private String id;

    private String url;

    private String date;

    private String parentId;

    private int size;

    private String type;

}
