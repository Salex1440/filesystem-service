package com.example.filesystemservice.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NodeRepository extends CrudRepository<Node, String> {

    @Query("SELECT n FROM Node n WHERE n.id= :id")
    Node findNodeById(@Param("id") String id);

    @Query("SELECT n FROM Node n WHERE n.parentId= :parentId")
    List<Node> findNodesByParentId(@Param("parentId") String parentId);

    @Query("SELECT n FROM Node n WHERE n.date <= upDate AND n.date >= lowDate")
    List<Node> findUpdatedNodes(@Param("lowDate") LocalDateTime lowDate,
                                @Param("upDate") LocalDateTime upDate);
    
}
