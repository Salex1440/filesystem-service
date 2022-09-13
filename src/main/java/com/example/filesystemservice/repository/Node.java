package com.example.filesystemservice.repository;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.repository.Temporal;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.TemporalType;
import java.util.Date;
import java.time.LocalDateTime;
import java.util.List;

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
