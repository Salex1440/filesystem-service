package com.example.filesystemservice.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class BatchDto {

    private List<ItemDto> items;

    private String updateDate;

}
