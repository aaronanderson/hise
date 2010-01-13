package org.apache.hise.dao;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Job extends JpaBase {
    @Id
    @GeneratedValue
    private Long id;
    
    private Date fire;
    
    @ManyToOne
    private Task task;
    
    private String action;
    private String details;
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Date getFire() {
        return fire;
    }
    public void setFire(Date fire) {
        this.fire = fire;
    }
    
    public Task getTask() {
        return task;
    }
    public void setTask(Task task) {
        this.task = task;
    }
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
    
    public String getDetails() {
        return details;
    }
    public void setDetails(String details) {
        this.details = details;
    }
    @Override
    public Object[] getKeys() {
        return new Object[] {id};
    }
}
