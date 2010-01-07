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
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;

import org.apache.hise.runtime.Task;

import org.apache.hise.dao.GenericHumanRole;
import org.apache.hise.dao.Person;
import org.apache.hise.engine.HISEEngine;
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
import org.apache.hise.lang.xsd.htda.TTaskAbstract;
import org.apache.hise.lang.xsd.htda.TTaskQueryResultSet;
import org.apache.hise.lang.xsd.htdt.IllegalState;
import org.apache.hise.lang.xsd.htdt.TTime;

/**
 * Implementation of WS-HT API. Operations are executed by end users, i.e. actual or potential owners. The identity of the user is implicitly passed when invoking any of the operations listed in the table below. The participant operations listed below only apply to tasks unless explicitly noted otherwise. The authorization column indicates people of which roles are authorized to perform the operation. Stakeholders of the task are not mentioned explicitly. They have the same authorization rights as business administrators.
 * 
 * @author Witek Wołejszo
 * @author Warren Crossing
 */
@WebService
public class TaskOperationsImpl implements TaskOperations {

    private HISEEngine hiseEngine;

    private WebServiceContext context;

    @Resource
    public void setContext(WebServiceContext context) {
        this.context = context;
    }

    private String getUserString() {
        return context.getUserPrincipal().getName();
    }
    
    private Person loadUser() {
        return hiseEngine.loadUser(getUserString());
    }

    public void claim(String identifier) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
        Task task = hiseEngine.loadTask(Long.valueOf(identifier));

        task.claim(loadUser());
    }

    public List<org.apache.hise.lang.xsd.htda.TTask> getMyTasks(String taskType, String genericHumanRole, String workQueue, List<TStatus> status, String whereClause, String createdOnClause, Integer maxTasks) throws IllegalArgumentFault, IllegalStateFault {
        return null;
    }

    // private void translateIllegalStateException(HTException xHT) throws IllegalStateFault {
    // if (xHT instanceof org.apache.hise.lang.faults.HTIllegalStateException) {
    // IllegalState state = new IllegalState();
    //
    // state.setStatus(translateStatusAPI(((org.apache.hise.lang.faults.HTIllegalStateException)xHT).getExceptionInfo()));
    // throw new IllegalStateFault(xHT.getMessage(), state, xHT);
    // }
    // }
    //
    // private void translateIllegalAccessException(HTException xHT) throws IllegalAccessFault {
    // if (xHT instanceof org.apache.hise.lang.faults.HTIllegalAccessException) {
    // throw new IllegalAccessFault(xHT.getMessage(), ((org.apache.hise.lang.faults.HTIllegalAccessException)xHT).getExceptionInfo(), xHT);
    // }
    // }
    //
    // private void translateIllegalOperationException(HTException xHT) throws IllegalOperationFault {
    // if (xHT instanceof org.apache.hise.lang.faults.HTIllegalOperationException) {
    // throw new IllegalOperationFault(xHT.getMessage(), ((HTIllegalOperationException) xHT).getExceptionInfo(), xHT);
    // }
    // }
    //
    // private void translateIllegalArgumentException(HTException xHT) throws IllegalArgumentFault {
    // if (xHT instanceof org.apache.hise.lang.faults.HTIllegalArgumentException) {
    // throw new IllegalArgumentFault(xHT.getMessage(), ((org.apache.hise.lang.faults.HTIllegalArgumentException) xHT).getExceptionInfo(), xHT);
    // }
    // }
    //
    // private void translateRecipientNotAllowedException(HTException xHT) throws RecipientNotAllowed {
    // if (xHT instanceof org.apache.hise.lang.faults.HTRecipientNotAllowedException) {
    // throw new RecipientNotAllowed(xHT.getMessage(), ((org.apache.hise.lang.faults.HTRecipientNotAllowedException) xHT).getExceptionInfo(), xHT);
    // }
    // }
    //
    // private Long translateTaskIdentifier(String identifier) throws HTIllegalArgumentException {
    // if (null == identifier) {
    // throw new org.apache.hise.lang.faults.HTIllegalArgumentException("Must specific a Task id.","Id");
    // }
    //
    // try {
    // return Long.valueOf(identifier);
    // } catch (NumberFormatException xNF) {
    // throw new HTIllegalArgumentException("Task identifier must be a number.", "Id: " + identifier);
    // }
    // }

    // /**
    // * Translates a single task to TTask.
    // *
    // * @param task The input task object.
    // * @return The Human Task WebService API task object.
    // */
    // private org.apache.hise.lang.xsd.htda.TTask translateOneTaskAPI(Task task) {
    // org.apache.hise.lang.xsd.htda.TTask ttask = new org.apache.hise.lang.xsd.htda.TTask();
    //
    // ttask.setId(Long.toString(task.getId()));
    // ttask.setTaskType("TASK");
    // ttask.setName(task.getTaskName());
    // ttask.setStatus(this.translateStatusAPI(task.getStatus()));
    // ttask.setCreatedOn(task.getCreatedOn());
    // /*
    // ttask.setPriority(task.getPriority());
    // */
    // //ttask.setTaskInitiator(task.getCreatedBy());
    // /*ttask.setTaskStakeholders(task.getTaskStakeholders());
    // ttask.setPotentialOwners(task.getPotentialOwners());
    // ttask.setBusinessAdministrators(task.getBusinessAdministrators());
    // ttask.setActualOwner(task.getActualOwner());
    // ttask.setNotificationRecipients(task.getNotificationRecipients());
    // */
    // ttask.setCreatedBy(task.getCreatedBy().toString());
    // ttask.setActivationTime(task.getActivationTime());
    // ttask.setExpirationTime(task.getExpirationTime());
    // ttask.setIsSkipable(task.isSkippable());
    // /*ttask.setHasPotentialOwners(task.getHasPotentialOwners());
    // ttask.setStartByExists(task.getStartByExists());
    // ttask.setCompleteByExists(task.getCompleteByExists());
    // ttask.setPresentationName(task.getPresentationName());
    // ttask.setPresentationSubject(task.getPresentationSubject());
    // ttask.setRenderingMethodExists(task.getRenderingMethodExists());
    // ttask.setHasOutput(task.getHasOutput());
    // */
    //
    // //TODO implement cjeck
    // //ttask.setHasFault(null != task.getFault());
    // ttask.setHasFault(false);
    //
    // ttask.setHasAttachments(!task.getAttachments().isEmpty());
    // //ttask.setHasComments(!task.getComments().isEmpty());
    //
    // ttask.setEscalated(task.isEscalated());
    // return ttask;
    // }

    // private List<org.apache.hise.lang.xsd.htda.TTask> translateTaskAPI(List<Task> in) {
    // List<org.apache.hise.lang.xsd.htda.TTask> result = new ArrayList<org.apache.hise.lang.xsd.htda.TTask>();
    // for (Task task : in) {
    // result.add(this.translateOneTaskAPI(task));
    // }
    // return result;
    // }

    // private List<Task.Status> translateStatusAPI(List<TStatus> in) {
    // List<Task.Status> result = new ArrayList<Task.Status>();
    // for (TStatus status : in) {
    // result.add(Task.Status.fromValue(in.toString()));
    // }
    //
    // return result;
    // }

    // private TStatus translateStatusAPI(Task.Status in) {
    // return TStatus.fromValue(in.toString());
    // }

    /**
     * Gets task information by a given identifier.
     * 
     * @param identifier
     *            task identifier as a number
     * @return task info
     * @throws org.example.ws_ht.api.wsdl.IllegalArgumentFault
     *             the number format is invalid or the task does not exist
     */
    public org.apache.hise.lang.xsd.htda.TTask getTaskInfo(String identifier) throws IllegalArgumentFault {
        return null;
    }

    public TTaskQueryResultSet query(String selectClause, String whereClause, String orderByClause, Integer maxTasks, Integer taskIndexOffset) throws IllegalArgumentFault, IllegalStateFault {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Releases a task by its identifier.
     * 
     * @param identifier
     *            task identifier
     * @throws org.example.ws_ht.api.wsdl.IllegalArgumentFault
     *             Identifier is invalid
     * @throws org.example.ws_ht.api.wsdl.IllegalStateFault
     *             The current state of the task doesn't allow to release it
     * @throws org.example.ws_ht.api.wsdl.IllegalAccessFault
     *             The logged in user has no right to release the task
     */
    public void release(String identifier) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
    }

    public void start(String identifier) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
    }

    public void activate(String identifier) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub

    }

    public void addAttachment(String identifier, String name, String accessType, Object attachment) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub

    }

    public void addComment(String identifier, String text) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub

    }

    public void complete(String identifier, Object taskData) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub

    }

    public void delegate(String identifier, TOrganizationalEntity organizationalEntity) throws IllegalAccessFault, IllegalStateFault, RecipientNotAllowed, IllegalArgumentFault {
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

    public void fail(String identifier, String faultName, Object faultData) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault, IllegalOperationFault {
        // TODO Auto-generated method stub

    }

    public void forward(String identifier, TOrganizationalEntity organizationalEntity) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
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

    public void getFault(String identifier, Holder<String> faultName, Holder<Object> faultData) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault, IllegalOperationFault {
        // TODO Auto-generated method stub

    }

    public Object getInput(String identifier, String part) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub
        return null;
    }

    public List<TTaskAbstract> getMyTaskAbstracts(String taskType, String genericHumanRole, String workQueue, List<TStatus> status, String whereClause, String createdOnClause, Integer maxTasks) throws IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getOutput(String identifier, String part) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
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

    public String getTaskDescription(String identifier, String contentType) throws IllegalArgumentFault {
        // TODO Auto-generated method stub
        return null;
    }

    public void nominate(String identifier, TOrganizationalEntity organizationalEntity) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub

    }

    public void remove(String identifier) throws IllegalAccessFault, IllegalArgumentFault {
        // TODO Auto-generated method stub

    }

    public void resume(String identifier) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub

    }

    public void setFault(String identifier, String faultName, Object faultData) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault, IllegalOperationFault {
        // TODO Auto-generated method stub

    }

    public void setGenericHumanRole(String identifier, String genericHumanRole, TOrganizationalEntity organizationalEntity) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
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

    public void stop(String identifier) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub

    }

    public void suspend(String identifier) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub

    }

    public void suspendUntil(String identifier, TTime time) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub

    }

}