package org.apache.hise.api;

import javax.xml.namespace.QName;

import org.apache.hise.engine.store.HISEDD;
import org.apache.hise.engine.store.TaskDD;
import org.apache.hise.lang.TaskDefinition;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public interface HISEEngine {
    public static class TaskInfo {
        public String taskKey;
        public TaskDefinition taskDefinition;
        public HISEDD parent;
        public TaskDD dd;
    }
    
    public void registerTask(TaskInfo ti);
    public Node receive(QName portType, String operation, Element body, Node requestHeader);
}