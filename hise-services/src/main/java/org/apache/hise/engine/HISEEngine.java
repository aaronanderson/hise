package org.apache.hise.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.dao.AssigneeDao;
import org.apache.hise.dao.GenericHumanRole;
import org.apache.hise.dao.Person;
import org.apache.hise.dao.TaskDao;
import org.apache.hise.dao.Task.TaskTypes;
import org.apache.hise.engine.store.HISEDD;
import org.apache.hise.engine.store.TaskDD;
import org.apache.hise.lang.HumanInteractions;
import org.apache.hise.lang.TaskDefinition;
import org.apache.hise.runtime.Task;
import org.apache.hise.utils.DOMUtils;
import org.w3c.dom.Element;

public class HISEEngine {
    private static Log log = LogFactory.getLog(HISEEngine.class);
    
    public static class TaskInfo {
        public String taskKey;
        public TaskDefinition taskDefinition;
        public HISEDD parent;
        public TaskDD dd;
    }
    
    public final Map<String, QName> tasksMap = new HashMap<String, QName>();
    public final Map<QName, TaskInfo> tasks = new HashMap<QName, TaskInfo>();
    public AssigneeDao assigneeDao;
    public TaskDao taskDao;
    
    public AssigneeDao getAssigneeDao() {
        return assigneeDao;
    }

    public void setAssigneeDao(AssigneeDao assigneeDao) {
        this.assigneeDao = assigneeDao;
    }

    public TaskDao getTaskDao() {
        return taskDao;
    }

    public void setTaskDao(TaskDao taskDao) {
        this.taskDao = taskDao;
    }

    public static QName getCanonicalQName(QName q) {
        String ns = q.getNamespaceURI();
        ns = ns.endsWith("/") ? ns.substring(0, ns.length() - 1) : ns;
        return new QName(ns, q.getLocalPart());
    }
    
    public static String tasksKey(QName portType, String operation) {
        return getCanonicalQName(portType) + ";" + operation; 
    }

    public void registerTask(TaskInfo ti) {
        log.debug("registering route " + ti.taskKey + " -> " + ti.taskDefinition.getTaskName());
        
        if (tasks.containsKey(ti.taskDefinition.getTaskName()) || tasksMap.containsKey(ti.taskKey)) {
            throw new IllegalArgumentException("Unable to deploy " + ti + " is already deployed.");
        }
        
        tasksMap.put(ti.taskKey, ti.taskDefinition.getTaskName());
        tasks.put(ti.taskDefinition.getTaskName(), ti);
        
        log.debug("registered");
    }
    
    public TaskDefinition getTaskDefinition(QName taskName) {
        Validate.notNull(tasks.get(taskName), "" + taskName + " not found");
        return tasks.get(taskName).taskDefinition;
    }
    
    public QName getTaskName(QName portType, String operation) {
        QName n = tasksMap.get(tasksKey(portType, operation));
        Validate.notNull(n, "Task for " + portType + " " + operation + " not found in routing table.");
        return n;
    }
    
    public Task loadTask(Long taskId) {
        return null;
    }
    
    public void receive(QName portType, String operation, Element body, String createdBy) {
        QName taskName = getTaskName(portType, operation);
        assert(taskName != null);
        log.debug("routed " + portType + " " + operation + " -> " + taskName);
        Task.create(this, taskName, createdBy, DOMUtils.domToString(DOMUtils.getFirstElement(DOMUtils.getFirstElement(body))));
    }
    
//    /**
//     * {@inheritDoc}
//     */
//    public Task createTask(QName taskName, String createdBy, String requestXml) {
//
//        Validate.notNull(taskName);
//        Validate.notNull(requestXml);
//        
//        log.info("Creating task: " + taskName + " , createdBy: " + createdBy);
//
//        TaskDefinition taskDefinition = getTaskDefinition(taskName);
//
//        Task newTask = new Task();
//        newTask.init(taskDefinition, createdBy, requestXml);
//        return newTask;
//    }

    /**
     * {@inheritDoc}
     */
    public List<org.apache.hise.dao.Task> getMyTasks(String personName, TaskTypes taskType, GenericHumanRole genericHumanRole, String workQueue, List<org.apache.hise.dao.Task.Status> statuses,
            String whereClause, String orderByClause, String createdOnClause, Integer maxTasks, Integer offset) {

        Person person = null;

        if (workQueue == null) {
            person = this.assigneeDao.getPerson(personName);
        }

        return this.taskDao.getTasks(person, taskType, genericHumanRole, workQueue, statuses,
                whereClause, orderByClause, createdOnClause, maxTasks, offset);
    }
    
    public Person loadUser(String userId) {
        return assigneeDao.getPerson(userId);
    }
    
    public void registerHumanInteractions(HumanInteractions hi) {
        for (TaskDefinition d : hi.getTaskDefinitions().values()) {
        }
    }
}
