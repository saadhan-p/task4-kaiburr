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

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import java.util.concurrent.TimeUnit;

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
    
    public Task executeTask(String taskId) {
    Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));

    TaskExecution execution = new TaskExecution();
    execution.setStartTime(new Date());

    // This block creates and manages the connection to Kubernetes
    try (KubernetesClient client = new KubernetesClientBuilder().build()) {
        // Generate a unique name for our pod
        String podName = "task-runner-" + task.getId() + "-" + System.currentTimeMillis();

        // 1. Define the Pod we want to create
        final Pod pod = new PodBuilder()
                .withNewMetadata()
                    .withName(podName)
                .endMetadata()
                .withNewSpec()
                    .addNewContainer()
                        .withName("runner-container")
                        .withImage("busybox:latest") // A small, simple image with shell tools
                        .withCommand("/bin/sh", "-c", task.getCommand()) // This runs your task's command
                    .endContainer()
                    .withRestartPolicy("Never") // Ensures the pod stops after the command finishes
                .endSpec()
                .build();

        // 2. Create the Pod in the Kubernetes cluster
        client.pods().inNamespace("default").create(pod);

        // 3. Wait up to 1 minute for the Pod to complete
        client.pods().inNamespace("default").withName(podName)
                .waitUntilCondition(p -> "Succeeded".equals(p.getStatus().getPhase()) || "Failed".equals(p.getStatus().getPhase()), 1, TimeUnit.MINUTES);

        // 4. Get the command's output from the Pod's logs
        String podLogs = client.pods().inNamespace("default").withName(podName).getLog();
        execution.setOutput(podLogs);

        // 5. Clean up by deleting the Pod
        client.pods().inNamespace("default").withName(podName).delete();

    } catch (Exception e) {
        execution.setOutput("Error creating Kubernetes pod: " + e.getMessage());
        e.printStackTrace(); // Helps with debugging
    } finally {
        execution.setEndTime(new Date());
        task.getTaskExecutions().add(execution);
        taskRepository.save(task);
    }

    return task;
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