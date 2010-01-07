package org.apache.hise.engine.store;

import javax.xml.namespace.QName;

public class TaskDD {
    public QName taskName;
    public Object handler;
    public Object sender;
    
    public QName getTaskName() {
        return taskName;
    }
    public void setTaskName(QName taskName) {
        this.taskName = taskName;
    }
    public Object getHandler() {
        return handler;
    }
    public void setHandler(Object handler) {
        this.handler = handler;
    }
    public Object getSender() {
        return sender;
    }
    public void setSender(Object sender) {
        this.sender = sender;
    }
    
    
}
