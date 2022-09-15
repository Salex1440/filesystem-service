package com.example.filesystemservice.repository;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Getter
@Setter
public class Node {

    @Id
    private String id;

    private String type;

    private int size;

    private String url;

    private String parentId;

    private Date date;

}
