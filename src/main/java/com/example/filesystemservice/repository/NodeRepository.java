package com.example.filesystemservice.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface NodeRepository extends CrudRepository<Node, String> {

    @Query("SELECT n FROM Node n WHERE n.id= :id")
    Node findNodeById(@Param("id") String id);

    @Query("SELECT n FROM Node n WHERE n.parentId= :parentId")
    List<Node> findNodesByParentId(@Param("parentId") String parentId);

    List<Node> findByDateBetween(Date dateStart, Date dateEnd);

}
