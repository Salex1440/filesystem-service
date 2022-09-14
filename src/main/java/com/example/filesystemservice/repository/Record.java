package com.example.filesystemservice.repository;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Setter
@Getter
public class Record {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    private String nodeId;

    private String nodeParentId;

    private String url;

    private Date updateDate;

    private int size;

    private String type;

}
