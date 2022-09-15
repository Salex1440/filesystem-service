package com.example.filesystemservice.repository;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Setter
@Getter
@NoArgsConstructor
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

    public Record(Node node) {
        nodeId = node.getId();
        nodeParentId = node.getParentId();
        url = node.getUrl();
        updateDate = node.getDate();
        size = node.getSize();
        type = node.getType();
    }

}
