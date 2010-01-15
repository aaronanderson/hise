package org.apache.hise.dao;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.hise.lang.xsd.htda.TStatus;

public class TaskQuery {
    private String user;
    private Collection<String> userGroups = new HashSet<String>();
    private String taskType = "ALL";
    private GenericHumanRole genericHumanRole = GenericHumanRole.ACTUALOWNER;
    private String workQueue = "";
    private List<TStatus> status = Collections.EMPTY_LIST;
    private String whereClause = "";
    private String createdOnClause = "";
    private Integer maxTasks = 20;
    
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public Collection<String> getUserGroups() {
        return userGroups;
    }
    public void setUserGroups(Collection<String> userGroups) {
        this.userGroups = userGroups;
    }
    public String getTaskType() {
        return taskType;
    }
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
    public GenericHumanRole getGenericHumanRole() {
        return genericHumanRole;
    }
    public void setGenericHumanRole(GenericHumanRole genericHumanRole) {
        this.genericHumanRole = genericHumanRole;
    }
    public String getWorkQueue() {
        return workQueue;
    }
    public void setWorkQueue(String workQueue) {
        this.workQueue = workQueue;
    }
    public List<TStatus> getStatus() {
        return status;
    }
    public void setStatus(List<TStatus> status) {
        this.status = status;
    }
    public String getWhereClause() {
        return whereClause;
    }
    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }
    public String getCreatedOnClause() {
        return createdOnClause;
    }
    public void setCreatedOnClause(String createdOnClause) {
        this.createdOnClause = createdOnClause;
    }
    public Integer getMaxTasks() {
        return maxTasks;
    }
    public void setMaxTasks(Integer maxTasks) {
        this.maxTasks = maxTasks;
    }
}
