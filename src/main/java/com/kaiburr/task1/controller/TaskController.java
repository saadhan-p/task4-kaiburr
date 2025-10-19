package com.kaiburr.task1.controller;

import com.kaiburr.task1.model.Task;
import com.kaiburr.task1.model.TaskExecution;
import com.kaiburr.task1.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // Combined all necessary imports

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = "http://localhost:5173") // CORS Fix applied
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
    /**
     * GET /tasks - Get all tasks
     * GET /tasks?id=123 - Get task by ID
     */
    @GetMapping
    public ResponseEntity<?> getTasks(@RequestParam(required = false) String id) {
        try {
            if (id != null && !id.isEmpty()) {
                // Get single task by ID
                Optional<Task> task = taskService.getTaskById(id);
                if (task.isPresent()) {
                    return ResponseEntity.ok(task.get());
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Task not found with id: " + id);
                }
            } else {
                // Get all tasks
                List<Task> tasks = taskService.getAllTasks();
                return ResponseEntity.ok(tasks);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage());
        }
    }
    
    /**
     * GET /tasks/search?name=hello - Search tasks by name
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchTasks(@RequestParam String name) {
        try {
            List<Task> tasks = taskService.searchTasksByName(name);
            if (tasks.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No tasks found with name containing: " + name);
            }
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage());
        }
    }
    
    /**
     * PUT /tasks - Create or update a task
     */
    @PutMapping
    public ResponseEntity<?> createTask(@RequestBody Task task) {
        try {
            Task savedTask = taskService.createTask(task);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid task: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage());
        }
    }
    
    /**
     * DELETE /tasks/{id} - Delete a task by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable String id) {
        try {
            boolean deleted = taskService.deleteTask(id);
            if (deleted) {
                return ResponseEntity.ok("Task deleted successfully with id: " + id);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Task not found with id: " + id);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage());
        }
    }
    
    /**
     * PUT /tasks/{id}/execute - Execute a task's command
     */
    @PutMapping("/{id}/execute")
    public ResponseEntity<?> executeTask(@PathVariable String id) {
        try {
            // FIX: Changed local variable type from TaskExecution to Task
            Task updatedTask = taskService.executeTask(id); 
            // FIX: Returning the full updated Task object
            return ResponseEntity.ok(updatedTask);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Execution failed: " + e.getMessage());
        }
    }
}