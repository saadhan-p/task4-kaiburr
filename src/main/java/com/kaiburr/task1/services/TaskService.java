package com.kaiburr.task1.services;

import com.kaiburr.task1.model.Task;
import com.kaiburr.task1.model.TaskExecution;
import com.kaiburr.task1.util.CommandValidator;
import com.kaiburr.task1.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private CommandValidator commandValidator;
    
    /**
     * Get all tasks
     */
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
    
    /**
     * Get task by ID
     */
    public Optional<Task> getTaskById(String id) {
        return taskRepository.findById(id);
    }
    
    /**
     * Search tasks by name (contains)
     */
    public List<Task> searchTasksByName(String name) {
        return taskRepository.findByNameContaining(name);
    }
    
    /**
     * Create or update task
     */
public Task createTask(Task task) {
    // Correct way to use the validator
    if (!commandValidator.isCommandSafe(task.getCommand())) {
        throw new IllegalArgumentException("Command is not safe and is not allowed.");
    }

    // Initialize taskExecutions list if null
    if (task.getTaskExecutions() == null) {
        task.setTaskExecutions(new ArrayList<>());
    }

    return taskRepository.save(task);
}

    
    /**
     * Delete task by ID
     */
    public boolean deleteTask(String id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    /**
     * Execute task command and save execution result
     */
    // TaskService.java - executeTask method

    public Task executeTask(String taskId) throws Exception {
    
    // Declare the variable that will hold the final saved task
    Task updatedTaskResult; 

    // Find task
    Optional<Task> optionalTask = taskRepository.findById(taskId);
    if (!optionalTask.isPresent()) {
        throw new IllegalArgumentException("Task not found with id: " + taskId);
    }
    
    Task task = optionalTask.get();
    
    // Validate command again before execution
    if (!commandValidator.isCommandSafe(task.getCommand())) {
        throw new IllegalArgumentException("Cannot execute unsafe command");
    }
    
    // Create execution object
    TaskExecution execution = new TaskExecution();
    execution.setStartTime(new Date());
    
    try {
        // Execute command
        String output = executeShellCommand(task.getCommand());
        execution.setOutput(output);
        execution.setEndTime(new Date());
        
        // Add execution to task
        if (task.getTaskExecutions() == null) {
            task.setTaskExecutions(new ArrayList<>());
        }
        task.getTaskExecutions().add(execution);
        
        // Save updated task and capture the result
        updatedTaskResult = taskRepository.save(task);


    } catch (Exception e) {
        execution.setOutput("ERROR: " + e.getMessage());
        execution.setEndTime(new Date());
        
        // Add execution even if failed
        if (task.getTaskExecutions() == null) {
            task.setTaskExecutions(new ArrayList<>());
        }
        task.getTaskExecutions().add(execution);
        
        // Save the failed execution and capture the result before re-throwing
        updatedTaskResult = taskRepository.save(task);
        
        throw e;
    }

    // FIX: Return the variable that was actually assigned the saved Task object
    return updatedTaskResult;
}

    
    /**
     * Execute shell command and return output
     */
    private String executeShellCommand(String command) throws Exception {
        StringBuilder output = new StringBuilder();
        
        // Determine OS and use appropriate shell
        String[] shellCommand;
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("win")) {
            // Windows
            shellCommand = new String[]{"cmd.exe", "/c", command};
        } else {
            // Linux/Mac
            shellCommand = new String[]{"/bin/sh", "-c", command};
        }
        
        Process process = Runtime.getRuntime().exec(shellCommand);
        
        // Read output
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream())
        );
        
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        
        // Read error stream
        BufferedReader errorReader = new BufferedReader(
            new InputStreamReader(process.getErrorStream())
        );
        
        StringBuilder errorOutput = new StringBuilder();
        while ((line = errorReader.readLine()) != null) {
            errorOutput.append(line).append("\n");
        }
        
        // Wait for process to complete
        int exitCode = process.waitFor();
        
        if (exitCode != 0 && errorOutput.length() > 0) {
            throw new Exception("Command failed with exit code " + exitCode + ": " + errorOutput.toString());
        }
        
        return output.toString().trim();
    }
}