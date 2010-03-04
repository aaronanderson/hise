/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.hise.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.api.HISEEngine;
import org.apache.hise.api.HISEUserDetails;
import org.apache.hise.api.Handler;
import org.apache.hise.dao.HISEDao;
import org.apache.hise.dao.Job;
import org.apache.hise.engine.jaxws.HISEJaxWSClient;
import org.apache.hise.engine.store.HISEDD;
import org.apache.hise.engine.store.TaskDD;
import org.apache.hise.lang.TaskDefinition;
import org.apache.hise.runtime.Task;
import org.apache.hise.runtime.TaskEvaluator;
import org.apache.hise.utils.DOMUtils;
import org.apache.hise.utils.XQueryEvaluator;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class HISEEngineImpl implements HISEEngine {
    private static Log log = LogFactory.getLog(HISEEngineImpl.class);
    
    public final Map<String, QName> tasksMap = new HashMap<String, QName>();
    public final Map<QName, TaskInfo> tasks = new HashMap<QName, TaskInfo>();
    private HISEDao hiseDao;
    private HISEUserDetails hiseUserDetails;
    
    private HISEScheduler hiseScheduler;
    
    public HISEDao getHiseDao() {
        return hiseDao;
    }
    
    public void setHiseUserDetails(HISEUserDetails hiseUserDetails) {
        this.hiseUserDetails = hiseUserDetails;
    }

    public HISEUserDetails getHiseUserDetails() {
        return hiseUserDetails;
    }

    public void setHiseDao(HISEDao hiseDao) {
        this.hiseDao = hiseDao;
    }
    
    public HISEScheduler getHiseScheduler() {
        return hiseScheduler;
    }

    public void setHiseScheduler(HISEScheduler hiseScheduler) {
        this.hiseScheduler = hiseScheduler;
    }

    public static QName getCanonicalQName(QName q) {
        String ns = q.getNamespaceURI();
        ns = ns.endsWith("/") ? ns.substring(0, ns.length() - 1) : ns;
        return new QName(ns, q.getLocalPart());
    }
    
    public static String tasksKey(Handler handler, QName portType, String operation) {
        return "" + System.identityHashCode(handler) + ";" + getCanonicalQName(portType) + ";" + operation; 
    }

    public void registerTask(TaskInfo ti) {
        TaskDefinition d = ti.taskDefinition;
        String taskKey = HISEEngineImpl.tasksKey(ti.dd.getHandler(), d.getTaskInterface().getPortType(), d.getTaskInterface().getOperation());
        log.debug("registering route " + taskKey + " -> " + ti.taskDefinition.getTaskName());
        
        QName taskName = ti.taskDefinition.getTaskName();
        if (tasks.containsKey(taskName) || tasksMap.containsKey(taskKey)) {
            log.warn("Unable to deploy " + ti + " is already deployed.");
        }
        
        tasksMap.put(taskKey, ti.taskDefinition.getTaskName());
        tasks.put(ti.taskDefinition.getTaskName(), ti);
        
        log.debug("registered");
    }
    
    public TaskDefinition getTaskDefinition(QName taskName) {
        Validate.notNull(tasks.get(taskName), "" + taskName + " not found");
        return tasks.get(taskName).taskDefinition;
    }
    
    public QName getTaskName(Handler handler, QName portType, String operation) {
    	java.util.logging.Logger l;
    	String key = tasksKey(handler, portType, operation);
        QName n = tasksMap.get(key);
        Validate.notNull(n, "Task for " + portType + " " + operation + " not found in routing table. Key: " + key);
        return n;
    }
    
    public static String fetchCreatedBy(Node requestHeader) {
        log.debug("header " + DOMUtils.domToString(requestHeader));
        XQueryEvaluator e = new XQueryEvaluator();
        List r = e.evaluateExpression("declare namespace htd='http://www.example.org/WS-HT'; xs:string(*/htd:initiator)", requestHeader);
        return r.size() == 1 ? (String) r.get(0) : "";
    }
    
    public Node receive(Handler handler, QName portType, String operation, Element body, Node requestHeader) {
        String createdBy = fetchCreatedBy(requestHeader);
        
        QName taskName = getTaskName(handler, portType, operation);
        assert(taskName != null);
        log.debug("routed " + portType + " " + operation + " -> " + taskName);
        TaskDefinition def = getTaskDefinition(taskName);
        Task t;
        if (def.isNotification()) {
            t = Task.createNotification(this, getTaskDefinition(taskName), createdBy, DOMUtils.getFirstElement(body), requestHeader);
        } else {
            t = Task.create(this, getTaskDefinition(taskName), createdBy, DOMUtils.getFirstElement(body), requestHeader);
        }
        return t.getTaskEvaluator().evaluateApproveResponseHeader();
    }
    
    public void receiveNotification(QName notificationName, Node request) {
        notificationName = DOMUtils.uniqueQName(notificationName);
        log.debug("Receiving notification " + notificationName);
        TaskInfo n = tasks.get(notificationName);
        Validate.notNull(n, "Can't find notification in registry " + notificationName);
        Validate.isTrue(n.taskDefinition.isNotification());
        Task.createNotification(this, n.taskDefinition, "", request, TaskEvaluator.defaultHeader());
    }
    
    public void sendResponse(QName taskName, Node body, Node epr) {
        TaskInfo ti = tasks.get(taskName);
        log.debug("sending response for " + taskName + " to " + DOMUtils.domToString(epr) + " body " + DOMUtils.domToString(body) + " " + ti.dd.sender);
        HISEJaxWSClient c = (HISEJaxWSClient) ti.dd.sender;
        log.debug("result: " + c.invoke(body, epr));
    }
    
    
    public void executeJob(Job job) {
        Task t = Task.load(this, job.getTask().getId());
        try {
            t.setCurrentJob(job);
            t.getClass().getMethod(job.getAction() + "JobAction").invoke(t);
        } catch (Exception e) {
            throw new RuntimeException("timer job failed", e);
        }
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

//    /**
//     * {@inheritDoc}
//     */
//    public List<org.apache.hise.dao.Task> getMyTasks(String personName, TaskTypes taskType, GenericHumanRole genericHumanRole, String workQueue, List<org.apache.hise.dao.Task.Status> statuses,
//            String whereClause, String orderByClause, String createdOnClause, Integer maxTasks, Integer offset) {
//
//        Person person = null;
//
//        EntityManager em = entityManagerFactory.createEntityManager();
//        if (workQueue == null) {
//            person = new AssigneeDao(em).getPerson(personName);
//        }
//
//        return new TaskDao(em).getTasks(person, taskType, genericHumanRole, workQueue, statuses,
//                whereClause, orderByClause, createdOnClause, maxTasks, offset);
//    }
    
//    public Person loadUser(String userId, EntityManager em) {
//        return new AssigneeDao(em).getPerson(userId);
//    }
}
