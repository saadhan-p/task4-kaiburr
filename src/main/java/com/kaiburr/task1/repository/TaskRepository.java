package com.kaiburr.task1.repository;

import com.kaiburr.task1.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {
    
    // Find tasks where name contains the given string (case-insensitive)
    List<Task> findByNameContaining(String name);
    
    // Alternative: case-insensitive search
    List<Task> findByNameContainingIgnoreCase(String name);
}