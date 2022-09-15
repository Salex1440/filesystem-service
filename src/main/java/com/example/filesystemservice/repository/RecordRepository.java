package com.example.filesystemservice.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;

import java.util.Date;
import java.util.List;

public interface RecordRepository extends CrudRepository<Record, Integer> {

    @Query("SELECT r FROM Record r WHERE r.nodeId= :nodeId AND r.updateDate >= :from AND r.updateDate < :to")
    List<Record> findByNodeIdBetweenDate(@NonNull String nodeId, Date from, Date to);

    List<Record> findByNodeId(@NonNull String nodeId);

}
