package org.apache.hise.engine;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.hise.dao.AssigneeDao;
import org.apache.hise.dao.TaskDao;
import org.apache.hise.lang.TaskDefinition;

public class HISEEngine {
    public final Map<String, QName> tasksMap = new HashMap<String, QName>();
    public final Map<QName, TaskDetails> tasks = new HashMap<QName, TaskDetails>();
    public AssigneeDao assigneeDao;
    public TaskDao taskDao;
    public HumanTaskServicesImpl humanTaskServices;
    
    public static QName getCanonicalQName(QName q) {
        String ns = q.getNamespaceURI();
        ns = ns.endsWith("/") ? ns.substring(0, ns.length() - 1) : ns;
        return new QName(ns, q.getLocalPart());
    }
    
    public static String tasksKey(QName portType, String operation) {
        return getCanonicalQName(portType) + ";" + operation; 
    }
    
    public void deploy(HISEDeploymentInfo di) {
        
    }
    
    public TaskDefinition getTaskDefinition(QName taskName) {
        return tasks.get(taskName).taskDefinition;
    }
    
    public QName getTaskName(QName portType, String operation) {
        return tasksMap.get(tasksKey(portType, operation));
    }
}
