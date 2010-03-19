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

package org.apache.hise.engine.jaxws;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.jws.WebService;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;

import org.apache.hise.dao.GenericHumanRole;
import org.apache.hise.dao.TaskQuery;
import org.apache.hise.engine.HISEEngineImpl;

import org.apache.hise.engine.wsdl.IllegalAccessFault;
import org.apache.hise.engine.wsdl.IllegalArgumentFault;
import org.apache.hise.engine.wsdl.IllegalOperationFault;
import org.apache.hise.engine.wsdl.IllegalStateFault;

import org.apache.hise.engine.wsdl.RecipientNotAllowed;

import org.apache.hise.engine.wsdl.TaskOperations;

import org.apache.hise.lang.xsd.htd.TOrganizationalEntity;

import org.apache.hise.lang.xsd.htda.TAttachment;
import org.apache.hise.lang.xsd.htda.TAttachmentInfo;
import org.apache.hise.lang.xsd.htda.TComment;
import org.apache.hise.lang.xsd.htda.TStatus;
import org.apache.hise.lang.xsd.htda.TTask;
import org.apache.hise.lang.xsd.htda.TTaskAbstract;
import org.apache.hise.lang.xsd.htda.TTaskQueryResultSet;

import org.apache.hise.lang.xsd.htdt.TTime;
import org.apache.hise.runtime.Task;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of WS-HT API. Operations are executed by end users, i.e.
 * actual or potential owners. The identity of the user is implicitly passed
 * when invoking any of the operations listed in the table below. The
 * participant operations listed below only apply to tasks unless explicitly
 * noted otherwise. The authorization column indicates people of which roles are
 * authorized to perform the operation. Stakeholders of the task are not
 * mentioned explicitly. They have the same authorization rights as business
 * administrators.
 * 
 * @author Witek Wołejszo
 * @author Warren Crossing
 */
@Transactional
@WebService
public class TaskOperationsImpl implements TaskOperations {

    private HISEEngineImpl hiseEngine;

    private WebServiceContext context;

    /**
     * Sets up {@link WebServiceContext} used to lookup authenticated user
     * performing operations.
     * 
     * @throws Exception
     */
    public void init() throws Exception {
        context = (WebServiceContext) Class.forName("org.apache.cxf.jaxws.context.WebServiceContextImpl").newInstance();
    }

    /**
     * IoC setter.
     * 
     * @param hiseEngine
     */
    public void setHiseEngine(HISEEngineImpl hiseEngine) {
        this.hiseEngine = hiseEngine;
    }

    protected String getUserString() {
        return context.getUserPrincipal().getName();
    }

    // implementation in progress

    /**
     * {@inheritDoc}
     */
    public List<TTask> getMyTasks(String taskType, String genericHumanRole, String workQueue, List<TStatus> status, String whereClause, String createdOnClause,
            Integer maxTasks) throws IllegalArgumentFault, IllegalStateFault {

        List<TTask> result = new ArrayList<TTask>();

        String user = getUserString();

        TaskQuery query = new TaskQuery();
        query.setUser(user);
        query.setUserGroups(hiseEngine.getHiseUserDetails().getUserGroups(user));
        query.setTaskType(taskType);
        query.setGenericHumanRole(GenericHumanRole.valueOf(genericHumanRole));
        query.setWorkQueue(workQueue);
        query.setStatus(status);
        query.setWhereClause(whereClause);
        query.setCreatedOnClause(createdOnClause);

        if (maxTasks != null) {
            query.setMaxTasks(maxTasks);
        }

        List<org.apache.hise.dao.Task> tasks = hiseEngine.getHiseDao().getUserTasks(query);
        for (org.apache.hise.dao.Task u : tasks) {
            TTask t = convertTask(u.getId());
            result.add(t);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    public Object getInput(String identifier, String part) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        Task t = Task.load(hiseEngine, Long.parseLong(identifier));
        t.setCurrentUser(getUserString());
        return t.getInput(part);
    }

    /**
     * {@inheritDoc}
     */
    public Object getOutput(String identifier, String part) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        Task t = Task.load(hiseEngine, Long.parseLong(identifier));
        t.setCurrentUser(getUserString());
        return t.getOutput(part);
    }

    public void stop(String identifier) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        Task t = Task.load(hiseEngine, Long.parseLong(identifier));
        t.setCurrentUser(getUserString());
        t.stop();
    }

    public void suspend(String identifier) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        Task t = Task.load(hiseEngine, Long.parseLong(identifier));
        t.setCurrentUser(getUserString());
        t.suspend();
    }

    public void suspendUntil(String identifier, TTime time) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        Task t = Task.load(hiseEngine, Long.parseLong(identifier));
        t.setCurrentUser(getUserString());
        Date when = time.getPointOfTime();
        if (when == null) {
            Duration when2 = time.getTimePeriod();
            when = Calendar.getInstance().getTime();
            when2.addTo(when);
        }

        t.suspendUntil(when);
    }

    public void remove(String identifier) throws IllegalAccessFault, IllegalArgumentFault {
        Task t = Task.load(hiseEngine, Long.parseLong(identifier));
        t.setCurrentUser(getUserString());
        t.remove();
    }

    public void resume(String identifier) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // OrgEntity user = loadUser();
        Task t = Task.load(hiseEngine, Long.parseLong(identifier));
        t.setCurrentUser(getUserString());
        t.resume();
    }

    public org.apache.hise.lang.xsd.htda.TTask getTaskInfo(String identifier) throws IllegalArgumentFault {
        return convertTask(hiseEngine.getHiseDao().find(org.apache.hise.dao.Task.class, Long.parseLong(identifier)).getId());
    }

    public void claim(String identifier) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
        Task task = Task.load(hiseEngine, Long.valueOf(identifier));
        task.setCurrentUser(getUserString());
        task.claim();
    }

    public void fail(String identifier, String faultName, Object faultData) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault,
            IllegalOperationFault {
        Task t = Task.load(hiseEngine, Long.parseLong(identifier));
        t.setCurrentUser(getUserString());
        t.fail();
    }

    public void forward(String identifier, TOrganizationalEntity organizationalEntity) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        Task t = Task.load(hiseEngine, Long.parseLong(identifier));
        t.setCurrentUser(getUserString());
        t.forward(organizationalEntity);
    }

    public String getTaskDescription(String identifier, String contentType) throws IllegalArgumentFault {
        Task t = Task.load(hiseEngine, Long.parseLong(identifier));
        t.setCurrentUser(getUserString());
        return t.getTaskEvaluator().evalPresentationDescription();
    }

    public void release(String identifier) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
        Task t = Task.load(hiseEngine, Long.parseLong(identifier));
        t.setCurrentUser(getUserString());
        t.release();
    }

    public void start(String identifier) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
        Task t = Task.load(hiseEngine, Long.parseLong(identifier));
        t.setCurrentUser(getUserString());
        t.start();
    }

    public void complete(String identifier, Object taskData) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        Task t = Task.load(hiseEngine, Long.parseLong(identifier));
        t.setCurrentUser(getUserString());
        t.complete();
    }
    
    // not started

    public TTaskQueryResultSet query(String selectClause, String whereClause, String orderByClause, Integer maxTasks, Integer taskIndexOffset)
            throws IllegalArgumentFault, IllegalStateFault {
        // TODO Auto-generated method stub
        return null;
    }

    public void activate(String identifier) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub

    }

    public void addAttachment(String identifier, String name, String accessType, Object attachment) throws IllegalAccessFault, IllegalStateFault,
            IllegalArgumentFault {
        // TODO Auto-generated method stub

    }

    public void addComment(String identifier, String text) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub

    }
    
    public void delegate(String identifier, TOrganizationalEntity organizationalEntity) throws IllegalAccessFault, IllegalStateFault, RecipientNotAllowed,
            IllegalArgumentFault {
        // TODO Auto-generated method stub

    }

    public void deleteAttachments(String identifier, String attachmentName) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub

    }

    public void deleteFault(String identifier) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub

    }

    public void deleteOutput(String identifier) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub

    }

    public List<TAttachmentInfo> getAttachmentInfos(String identifier) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub
        return null;
    }

    public List<TAttachment> getAttachments(String identifier, String attachmentName) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub
        return null;
    }

    public List<TComment> getComments(String identifier) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getRendering(Object identifier, QName renderingType) throws IllegalArgumentFault {
        // TODO Auto-generated method stub
        return null;
    }

    public List<QName> getRenderingTypes(Object identifier) throws IllegalArgumentFault {
        // TODO Auto-generated method stub
        return null;
    }

    public void nominate(String identifier, TOrganizationalEntity organizationalEntity) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub

    }

    public void setFault(String identifier, String faultName, Object faultData) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault,
            IllegalOperationFault {
        // TODO Auto-generated method stub

    }

    public void setGenericHumanRole(String identifier, String genericHumanRole, TOrganizationalEntity organizationalEntity) throws IllegalAccessFault,
            IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub

    }

    public void setOutput(String identifier, String part, Object taskData) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub

    }

    public void setPriority(String identifier, BigInteger priority) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub

    }

    public void skip(String identifier) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault, IllegalOperationFault {
        // TODO Auto-generated method stub
    }

    public void getFault(String identifier, Holder<String> faultName, Holder<Object> faultData) throws IllegalArgumentFault, IllegalStateFault,
            IllegalOperationFault, IllegalAccessFault {
        // TODO Auto-generated method stub

    }

    public List<TTaskAbstract> getMyTaskAbstracts(String taskType, String genericHumanRole, String workQueue, List<TStatus> status, String whereClause,
            String createdOnClause, Integer maxTasks) throws IllegalArgumentFault, IllegalStateFault {
        // TODO Auto-generated method stub
        return null;
    }

    // Util

    private TTask convertTask(Long id) {
        Task task = Task.load(hiseEngine, id);
        org.apache.hise.dao.Task taskDto = task.getTaskDto();

        TTask result = new TTask();
        result.setId(taskDto.getId().toString());
        result.setTaskType(taskDto.isNotification() ? "NOTIFICATION" : "TASK");
        result.setCreatedOn(taskDto.getCreatedOn());
        result.setActivationTime(taskDto.getActivationTime());
        if (taskDto.getActualOwner() != null) {
            result.setActualOwner(taskDto.getActualOwner());
        }
        result.setCreatedBy(taskDto.getCreatedBy());
        result.setPresentationName(task.getTaskEvaluator().getPresentationName());
        result.setPresentationSubject(task.getTaskEvaluator().evalPresentationSubject());
        result.setName(taskDto.getTaskDefinitionName());
        result.setStatus(TStatus.fromValue(taskDto.getStatus().toString()));

        return result;
    }

}