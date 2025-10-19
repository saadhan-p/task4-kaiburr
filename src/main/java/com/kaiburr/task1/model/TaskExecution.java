package com.kaiburr.task1.model;

import java.util.Date;



public class TaskExecution {
    
    private Date startTime;
    private Date endTime;
    private String output;
    
    // Constructors
    public TaskExecution() {
    }
    
    public TaskExecution(Date startTime, Date endTime, String output) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.output = output;
    }
    
    // Getters and Setters
    public Date getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    
    public Date getEndTime() {
        return endTime;
    }
    
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    
    public String getOutput() {
        return output;
    }
    
    public void setOutput(String output) {
        this.output = output;
    }
    
    @Override
    public String toString() {
        return "TaskExecution{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", output='" + output + '\'' +
                '}';
    }
}