package com.example.filesystemservice.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface NodeRepository extends CrudRepository<Node, String> {

    @Query("SELECT n FROM Node n WHERE n.di= :id")
    Node findNodeById(@Param("id") String id);
    
}
