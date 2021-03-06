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

package org.apache.hise.runtime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.dao.GenericHumanRole;
import org.apache.hise.dao.HISEDao;
import org.apache.hise.dao.Job;
import org.apache.hise.dao.Message;
import org.apache.hise.dao.TaskOrgEntity;
import org.apache.hise.dao.Task.Status;
import org.apache.hise.dao.TaskOrgEntity.OrgEntityType;
import org.apache.hise.engine.HISEEngineImpl;
import org.apache.hise.lang.TaskDefinition;
import org.apache.hise.lang.xsd.htd.TGrouplist;
import org.apache.hise.lang.xsd.htd.TOrganizationalEntity;
import org.apache.hise.lang.xsd.htd.TUserlist;
import org.apache.hise.utils.DOMUtils;
import org.apache.hise.utils.XmlUtils;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Holds task instance information. Provides task business operations.
 * 
 * @author Kamil Eisenbart
 * @author Witek Wołejszo
 * @author Mateusz Lipczyński
 * @author Warren Crossing
 */
public class Task {

    private final Log __log = LogFactory.getLog(Task.class);

    private HISEEngineImpl hiseEngine;

    private org.apache.hise.dao.Task taskDto;

    private TaskDefinition taskDefinition;

    private TaskEvaluator taskEvaluator;

    private List<TaskStateListener> taskStateListeners;

    private Job currentJob;
    private Date currentEventDateTime = Calendar.getInstance().getTime();

    private String currentUser;

    private DeadlineController deadlineController;

    protected Task() {}

    public Job getCurrentJob() {
        return currentJob;
    }

    public void setCurrentJob(Job currentJob) {
        this.currentJob = currentJob;
    }

    public Date getCurrentEventDateTime() {
        return currentEventDateTime;
    }

    public void setCurrentEventDateTime(Date currentEventDateTime) {
        this.currentEventDateTime = currentEventDateTime;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    /**
     * TODO throw an exception if current user is not TASK_ADMINISTRATOR
     * @param currentUser
     */
    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    public HISEEngineImpl getHiseEngine() {
        return hiseEngine;
    }

    private Task(HISEEngineImpl engine, boolean notification) {

        Validate.notNull(engine);

        this.hiseEngine = engine;

        taskStateListeners = new ArrayList<TaskStateListener>();
        if (!notification) {
            taskStateListeners.add(new TaskLifecycle(this));
            deadlineController = new DeadlineController(this);
            taskStateListeners.add(deadlineController);
        }

        taskEvaluator = new TaskEvaluator(this);
    }

    public org.apache.hise.dao.Task getTaskDto() {
        return taskDto;
    }

    public void setTaskDto(org.apache.hise.dao.Task taskDto) {
        this.taskDto = taskDto;
    }

    public TaskEvaluator getTaskEvaluator() {
        return taskEvaluator;
    }

    public void setTaskEvaluator(TaskEvaluator taskEvaluator) {
        this.taskEvaluator = taskEvaluator;
    }

    public static Task load(HISEEngineImpl engine, Long id) {
        HISEDao dao = engine.getHiseDao();
        org.apache.hise.dao.Task dto = dao.find(org.apache.hise.dao.Task.class, id);
        Task t = new Task(engine, dto.isNotification());
        t.taskDto = dto;
        t.taskDefinition = engine.getTaskDefinition(t.taskDto.getTaskDefinitionName());
        return t;
    }

    private void tryNominateOwner() throws HiseIllegalStateException {
        {
            int poSize = 0;
            TaskOrgEntity selected = null;
            for (TaskOrgEntity e : taskDto.getPeopleAssignments()) {
                if (e.getGenericHumanRole() == GenericHumanRole.POTENTIALOWNERS) {
                    poSize ++;
                    if (e.getType() == TaskOrgEntity.OrgEntityType.USER) {
                        selected = e;
                    }
                }
            }

            if (poSize == 1 && selected != null) {
                //Nominate a single potential owner
                setActualOwner(selected.getName());
            }
        }
    }

    public static Task create(HISEEngineImpl engine, TaskDefinition taskDefinition, String createdBy, Node requestXml, Node requestHeader) {

        Task t = new Task(engine, false);
        Validate.notNull(taskDefinition);
        Validate.isTrue(!taskDefinition.isNotification());

        Map<String, Node> inputParts = findInputParts(taskDefinition, requestXml);

        t.taskDefinition = taskDefinition;
        org.apache.hise.dao.Task taskDto = new org.apache.hise.dao.Task();
        taskDto.setTaskDefinitionKey(taskDefinition.getTaskName().toString());
        taskDto.setCreatedBy(createdBy);
        taskDto.setStatus(null);
        for(String partName: inputParts.keySet()) {
            taskDto.getInput().put(partName, new Message(partName, DOMUtils.domToString(inputParts.get(partName))));
        }
        taskDto.getInput().put("requestHeader", new Message("requestHeader", DOMUtils.domToString(requestHeader)));
        taskDto.setCreatedOn(new Date());
        taskDto.setActivationTime(new Date());
        taskDto.setEscalated(false);
        taskDto.setNotification(false);

        engine.getHiseDao().persist(taskDto);
        t.taskDto = taskDto;
        try {
            t.setStatus(Status.CREATED);
        } catch (HiseIllegalStateException e) {
            throw new IllegalStateException(e);
        }

        taskDto.setPeopleAssignments(t.getTaskEvaluator().evaluatePeopleAssignments());

        try {
            t.setStatus(Status.READY);
        } catch (HiseIllegalStateException e) {
            throw new IllegalStateException(e);
        }

        try {
            t.tryNominateOwner();
        } catch (HiseIllegalStateException e) {
            t.__log.warn("Could not nominate owner.");
        }

        return t;

        // recalculatePresentationParameters();
        //        
        // //retrieveExistingAssignees check if group or people with the same name exist
        // //and retrieves existing entities
        // taskDto.setPotentialOwners(hiseEngine.assigneeDao.saveNotExistingAssignees(taskDefinition.evaluateHumanRoleAssignees(GenericHumanRole.POTENTIAL_OWNERS, this)));
        // taskDto.setBusinessAdministrators(hiseEngine.assigneeDao.saveNotExistingAssignees(taskDefinition.evaluateHumanRoleAssignees(GenericHumanRole.BUSINESS_ADMINISTRATORS, this)));
        // taskDto.setExcludedOwners(hiseEngine.assigneeDao.saveNotExistingAssignees(taskDefinition.evaluateHumanRoleAssignees(GenericHumanRole.EXCLUDED_OWNERS, this)));
        // taskDto.setNotificationRecipients(hiseEngine.assigneeDao.saveNotExistingAssignees(taskDefinition.evaluateHumanRoleAssignees(GenericHumanRole.NOTIFICATION_RECIPIENTS, this)));
        // taskDto.setTaskStakeholders(hiseEngine.assigneeDao.saveNotExistingAssignees(taskDefinition.evaluateHumanRoleAssignees(GenericHumanRole.TASK_STAKEHOLDERS, this)));
        //        
        // //TODO test
        // if (this.taskStakeholders.isEmpty()) {
        // __log.error("No task stakeholders. Wrong configuration. Cannot create task.");
        // throw new HTConfigurationException("No task stakeholders. Wrong configuration. Cannot create task.", null);
        // }
        //
        // this.addOperationComment(Operations.CREATE);
        //
        // this.createdBy = createdBy;
        // this.createdOn = new Date();
        // this.activationTime = new Date();
        // this.escalated = false;
        // this.notification = false;
        // this.setStatus(Status.CREATED);
        //       
        // Person nominatedPerson = this.nominateActualOwner(this.potentialOwners);
        // if (nominatedPerson != null) {
        //            
        // this.actualOwner = nominatedPerson;
        // this.addOperationComment(Operations.NOMINATE, nominatedPerson);
        // this.setStatus(Status.RESERVED);
        //            
        // } else if (!this.potentialOwners.isEmpty()) {
        //            
        // this.setStatus(Status.READY);
        //            
        // }
        //        
        // recalculatePresentationParameters();
        //        
        // recalculatePriority();
    }

    static Map<String, Node> findInputParts(TaskDefinition taskDefinition, Node requestXml) {
        Map<String, Node> inputParts = new HashMap<String, Node>();

        Operation operation = taskDefinition.getPortType().getOperation(taskDefinition.getTaskInterface().getOperation(), null, null);
        if(operation == null) {
            LogFactory.getLog(Task.class).error("Operation: " + taskDefinition.getTaskInterface().getOperation() + " not found in port type definition.");
            return inputParts;
        }

        Map<String, Part> partsMap = operation.getInput().getMessage().getParts();
        Node messagePart = null;
        for (Part part : partsMap.values()) {
            String name = part.getName();
            QName element = part.getElementName();
            QName type = part.getTypeName();
            Element root = (Element) requestXml;            
            XPath xPath = XPathFactory.newInstance().newXPath();
            try {
                if (element != null) {
                    Map namespaceMap = new HashMap(1);
                    namespaceMap.put("prefix", element.getNamespaceURI());
                    NamespaceContext nc = new NamespaceMap(namespaceMap);
                    xPath.setNamespaceContext(nc);
                    messagePart = (Node) xPath.evaluate("prefix:" + element.getLocalPart(), root, XPathConstants.NODE);
                } else if (type != null) {
                    messagePart = (Node) xPath.evaluate("child::*/" + part.getName(), root, XPathConstants.NODE);
                }
            } catch (XPathExpressionException ex) {
                LogFactory.getLog(Task.class).error("Can not get message part.", ex);
            }
            inputParts.put(name, messagePart);
        }

        return inputParts;
    }

    public static Task createNotification(HISEEngineImpl engine, TaskDefinition taskDefinition, String createdBy, Node requestXml, Node requestHeader) {

        Validate.notNull(taskDefinition);
        Validate.isTrue(taskDefinition.isNotification());

        Task t = new Task(engine, true);

        t.taskDefinition = taskDefinition;

        Map<String, Node> inputParts = findInputParts(taskDefinition, requestXml);

        org.apache.hise.dao.Task taskDto = new org.apache.hise.dao.Task();
        taskDto.setTaskDefinitionKey(taskDefinition.getTaskName().toString());
        taskDto.setCreatedBy(createdBy);
        taskDto.setStatus(null);
        for(String partName: inputParts.keySet()) {
            taskDto.getInput().put(partName, new Message(partName, DOMUtils.domToString(inputParts.get(partName))));
        }
        taskDto.getInput().put("requestHeader", new Message("requestHeader", DOMUtils.domToString(requestHeader)));
        taskDto.setCreatedOn(new Date());
        taskDto.setActivationTime(new Date());
        taskDto.setEscalated(false);
        taskDto.setNotification(true);
        engine.getHiseDao().persist(taskDto);

        t.taskDto = taskDto;
        try {
            t.setStatus(Status.CREATED);
        } catch (HiseIllegalStateException e) {
            throw new IllegalStateException(e);
        }

        taskDto.setPeopleAssignments(t.getTaskEvaluator().evaluatePeopleAssignments());

        try {
            t.setStatus(Status.READY);
        } catch (HiseIllegalStateException e) {
            throw new IllegalStateException(e);
        }

        engine.getHiseDao().persist(taskDto);

        return t;
    }

    public void setActualOwner(String user) throws HiseIllegalStateException {
        setStatus(Status.RESERVED);
        taskDto.setActualOwner(user);
    }

    public void setOutput(Node requestXml) {
        __log.debug("setting task output to: " + requestXml);
        this.taskDto.getOutput().put("request", new Message("request", DOMUtils.domToString(requestXml)));
    }

    public TaskDefinition getTaskDefinition() {
        return taskDefinition;
    }

    // /**
    // * If there is only one person in the given list, it
    // * returns this person. Otherwise, it returns null.
    // *
    // * @param assignees list of assignees that can contain persons and groups
    // * @return the only person in the list, otherwise null
    // */
    // protected final Person nominateActualOwner(Set<Assignee> assignees) {
    //
    // Validate.notNull(assignees);
    //
    // Person result = null;
    // int count = 0;
    // for (Assignee assignee : assignees) {
    // if (assignee instanceof Person) {
    // if (count++ > 0) {
    // break;
    // }
    // result = (Person)assignee;
    // }
    // }
    //
    // return (count == 1) ? result : null;
    // }
    //    
    // /**
    // * Returns task's name in a required language.
    // *
    // * @param lang subject language according ISO, e.g. en-US, pl, de-DE
    // * @return name
    // */
    // public String getName(String lang) {
    // Validate.notNull(lang);
    // return this.getTaskDefinition().getName(lang);
    // }
    //    
    // public QName getTaskName() {
    // return this.getTaskDefinition().getTaskName();
    // }
    //
    // /**
    // * Returns a formatted task subject in a required language.
    // *
    // * @param lang subject language according ISO, e.g. en-US, pl, de-DE
    // * @return subject
    // */
    // public String getSubject(String lang) {
    // Validate.notNull(lang);
    // return this.getTaskDefinition().getSubject(lang, this);
    // }
    //
    // /**
    // * Returns a formatted task description in a required language and form.
    // *
    // * @param lang The description language according ISO, e.g. en-US, pl, de-DE
    // * @param contentType The content type, text/plain for plain text or text/html for HTML-formatted text.
    // * @return description
    // */
    // public String getDescription(String lang, String contentType) {
    // Validate.notNull(lang);
    // Validate.notNull(contentType);
    // return this.getTaskDefinition().getDescription(lang, contentType, this);
    // }
    //
    // /**
    // * Recalculates priority. To be called after object creation or input message update.
    // */
    // private void recalculatePriority() {
    // __log.info("Recalculating priority.");
    // this.priority = this.getTaskDefinition().getPriority(this);
    // }
    //    
    // /**
    // * Recalculates presentation parameter values. To be called after object creation or
    // * input message update.
    // */
    // private void recalculatePresentationParameters() {
    //
    // __log.info("Recalculating presentation parameters.");
    //
    // Map<String, Object> pp = this.getTaskDefinition().getTaskPresentationParameters(this);
    //
    // //replace all calculated
    // for (Entry<String, Object> entry : pp.entrySet()) {
    //            
    // PresentationParameter p = this.presentationParameters.get(entry.getKey());
    //            
    // if (p != null) {
    //                
    // p.setValue(entry.getValue() == null ? null : entry.getValue());
    //
    // } else {
    //                
    // p = new PresentationParameter();
    // p.setTask(this);
    // p.setName(entry.getKey());
    // //TODO test
    // p.setValue(entry.getValue() == null ? null : entry.getValue());
    //
    // this.presentationParameters.put(p.getName(), p);
    // }
    // }
    //        
    // //remove obsolete from presentationParameters
    // Set<String> allKeys = this.presentationParameters.keySet();
    // for (String key : allKeys) {
    // if (!pp.containsKey(key)) {
    // allKeys.remove(key);
    // }
    // }
    // }
    //
    // /**
    // * Adds an attachment to the task.
    // *
    // * @param attachment a new attachment to add
    // */
    // public void addAttachment(Attachment attachment) {
    // Validate.notNull(attachment);
    // this.attachments.add(attachment);
    // //TODO addComment
    // }
    //

    public void setStatus(Status newStatus) throws HiseIllegalStateException {
        for (TaskStateListener l : taskStateListeners) {
            l.stateChanged(taskDto.getStatus(), newStatus);
        }
        taskDto.setStatus(newStatus);
    }

    // /***************************************************************
    // * Task operations *
    // ***************************************************************/
    //    
    // /**
    // * Sets Task status. Task status is changed indirectly by operations on tasks. Status
    // * change operation comment is added. If new status is SUSPENDED and transition is
    // * valid, current status is remembered in {@link Task#statusBeforeSuspend}.
    // *
    // * @param status The new {@link Status}.
    // * @throws HTIllegalStateException thrown when impossible transition is forced by the caller.
    // */
    // private void setStatus(Status status) throws HTIllegalStateException {
    //        
    // Validate.notNull(status);
    //
    // boolean isOk = false;
    //
    // // check if change is valid for current state
    // if (this.status != null) {
    //
    // switch (this.status) {
    //
    // case CREATED:
    // if (status == Status.READY || status == Status.RESERVED) {
    // isOk = true;
    // }
    //                
    // break;
    //
    // case READY:
    // if (status == Status.RESERVED || status == Status.IN_PROGRESS ||
    // status == Status.READY || status == Status.SUSPENDED) {
    // isOk = true;
    // }
    //
    // break;
    //
    // case RESERVED:
    // if (status == Status.IN_PROGRESS || status == Status.READY ||
    // status == Status.SUSPENDED || status == Status.RESERVED) {
    // isOk = true;
    // }
    //
    // break;
    //
    // case IN_PROGRESS:
    // if (status == Status.COMPLETED || status == Status.FAILED ||
    // status == Status.RESERVED || status == Status.READY ||
    // status == Status.SUSPENDED) {
    // isOk = true;
    // }
    //
    // break;
    //
    // default:
    // break;
    //
    // }
    //
    // if (isOk) {
    //
    // __log.debug("Changing Task status : " + this + " status from: " + this.status + " to: " + status);
    //
    // if (status.equals(Status.SUSPENDED)) {
    // this.statusBeforeSuspend = this.status;
    // } else {
    // Validate.isTrue(this.statusBeforeSuspend == null);
    // }
    //
    // this.addOperationComment(Operations.STATUS, status);
    // Status oldStatus = this.status;
    // this.status = status;
    //
    // for (TaskStateListener l : taskStateListeners) {
    // l.stateChanged(this, oldStatus, status);
    // }
    //
    // } else {
    //
    // __log.error("Changing Task status: " + this + " status from: " + this.status + " to: " + status + " is not allowed.");
    // throw new org.apache.hise.lang.faults.HTIllegalStateException("Changing Task's: " + this + " status from: " + this.status + " to: " + status
    // + " is not allowed, or task is SUSPENDED", status);
    // }
    //
    // } else {
    //
    // //TODO check allowed first statuses
    // __log.debug("Changing Task status: " + this + " status from: NULL to: " + status);
    // this.addOperationComment(Operations.STATUS, status);
    // this.status = status;
    //
    // for (TaskStateListener l : taskStateListeners) {
    // l.stateChanged(this, null, status);
    // }
    // }
    // }
    //    
    /**
     * Claims task. Task in READY status can be claimed by people from potential owners group not listed in excluded owners.
     * 
     * @throws HiseIllegalStateException Thrown when task is in illegal state for claim i.e. not READY.
     * @throws HiseIllegalAccessException Thrown when task is in illegal state for claim i.e. not READY or person cannot 
     *                                    become actual owner i.e. not potential owner or excluded.
     */
    public void claim() throws HiseIllegalStateException, HiseIllegalAccessException {

        if (taskDto.getActualOwner() != null) {
            throw new HiseIllegalStateException("Actual owner already set " + taskDto.getActualOwner());
        }

        if (!taskDto.getStatus().equals(org.apache.hise.dao.Task.Status.READY)) {
            throw new HiseIllegalStateException("Task not claimable. Not READY." + taskDto.getStatus());
        }

        // check if the task can be claimed by person
        if (isCurrentUserInPotentialOwners()) {
            throw new HiseIllegalAccessException("User: " + currentUser + " is not a potential owner.");
        }

        // //TODO test
        // // check if the person is excluded from potential owners
        // if ((this.getExcludedOwners() != null && this.getExcludedOwners().contains(person))) {
        // throw new HTIllegalAccessException("Person is excluded from potential owners.", person.getName());
        // }

        taskDto.setActualOwner(currentUser);

        // taskDto.addOperationComment(Operations.CLAIM, person);
        setStatus(Status.RESERVED);
    }

    /**
     * TODO implement
     */
    private boolean isCurrentUserInPotentialOwners() {
        return true;
    }

    public void start() throws HiseIllegalStateException {
        setStatus(Status.IN_PROGRESS);
    }

    public void stop() throws HiseIllegalStateException {
        setStatus(Status.RESERVED);
    }

    public void release() throws HiseIllegalStateException {
        setStatus(Status.READY);
    }

    /**
     * Suspends the task.
     * @throws HiseIllegalStateException 
     */
    public void suspend() throws HiseIllegalStateException {
        setStatus(Status.SUSPENDED);
    }

    public void suspendUntil(Date when) throws HiseIllegalStateException {
        Validate.notNull(when);

        setStatus(Status.SUSPENDED);
        Job job = hiseEngine.getHiseScheduler().createJob(when, "suspendUntil", taskDto);
        taskDto.setSuspendUntil(job);
    }

    public void suspendUntilJobAction() throws HiseIllegalStateException {
        taskDto.setSuspendUntil(null);
        resume();
    }

    public void deadlineJobAction() throws HiseIllegalStateException {
        taskDto.getDeadlines().remove(getCurrentJob());
        deadlineController.deadlineCrossed(getCurrentJob());
    }

    public void resume() throws HiseIllegalStateException {
        setStatus(taskDto.getStatusBeforeSuspend());
    }

    public void fail() throws HiseIllegalStateException {
        setStatus(Status.FAILED);
        sendResponse();
    }

    /**
     * TODO Execution of the task finished successfully. If no output data is set the operation returns illegalArgumentFault.
     */
    public void complete() throws HiseIllegalStateException {
        setStatus(Status.COMPLETED);
        sendResponse();
    }

    /**
     * FIXME is outcome a reponse?
     */
    private void sendResponse() {
        try {
            Node response = taskEvaluator.evaluateOutcome(taskDto.getStatus() == Status.COMPLETED);
            hiseEngine.sendResponse(getTaskDefinition().getTaskName(),
                    response,
                    taskEvaluator.createEprFromHeader(DOMUtils.parse(taskDto.getInput().get("requestHeader").getMessage()).getDocumentElement()));
        } catch (Exception e) {
            throw new RuntimeException("Sending response failed", e);
        }
    }

    private void releaseOwner() throws HiseIllegalStateException {
        setStatus(Status.READY);
        taskDto.setActualOwner(null);
    }

    public void forward(TOrganizationalEntity target) throws HiseIllegalStateException {
        Set<TaskOrgEntity> e = new HashSet<TaskOrgEntity>();

        for (String user : XmlUtils.notNull(target.getUsers(), new TUserlist()).getUser()) {
            TaskOrgEntity x = new TaskOrgEntity();
            x.setGenericHumanRole(GenericHumanRole.POTENTIALOWNERS);
            x.setName(user);
            x.setType(OrgEntityType.USER);
            x.setTask(taskDto);
            e.add(x);
        }

        for (String group : XmlUtils.notNull(target.getGroups(), new TGrouplist()).getGroup()) {
            TaskOrgEntity x = new TaskOrgEntity();
            x.setGenericHumanRole(GenericHumanRole.POTENTIALOWNERS);
            x.setName(group);
            x.setType(OrgEntityType.GROUP);
            x.setTask(taskDto);
            e.add(x);
        }

        forward(e);
    }

    public void forward(Set<TaskOrgEntity> targets) throws HiseIllegalStateException {
        __log.debug("forwarding to " + targets);
        releaseOwner();

        for (TaskOrgEntity x : taskDto.getPeopleAssignments()) {
            x.setTask(null);
            hiseEngine.getHiseDao().remove(x);
        }
        taskDto.getPeopleAssignments().clear();
        taskDto.getPeopleAssignments().addAll(targets);

        tryNominateOwner();
    }


    //    
    // /**
    // * Releases the Task.
    // * @param person The person that is releasing the Task.
    // * @throws HTIllegalAccessException The person is not authorised to perform release operation.
    // * @throws HTIllegalStateException Task cannot be released.
    // * @see HumanTaskServices.releaseTask
    // */
    // public void release(Person person) throws HTIllegalAccessException, HTIllegalStateException {
    //        
    // Validate.notNull(person);
    // Validate.notNull(person.getId());
    //
    // //TODO test
    // if (this.actualOwner == null) {
    // throw new HTIllegalAccessException("Task without actual owner cannot be released.");
    // }
    //
    // //TODO test
    // if (!this.actualOwner.equals(person) && !this.getBusinessAdministrators().contains(person)) {
    // throw new HTIllegalAccessException("Calling person is neither the task's actual owner not business administrator");
    // }
    //
    // this.actualOwner = null;
    // this.addOperationComment(Operations.RELEASE, person);
    // this.setStatus(Status.READY);
    // }
    //    
    // /**
    // * Starts task.
    // * @see HumanTaskServices#startTask(Long, String)
    // * @param person
    // * @throws HTIllegalStateException
    // * @throws HTIllegalAccessException
    // */
    // public void start(Person person) throws HTIllegalStateException, HTIllegalAccessException {
    //        
    // Validate.notNull(person);
    // Validate.notNull(person.getId());
    //        
    // //only potential owner can start the task
    // if (!this.getPotentialOwners().contains(person)) {
    // throw new HTIllegalAccessException("This person is not permited to start the task.", person.toString());
    // }
    //
    // //ready
    // if (this.getStatus().equals(Status.READY)) {
    //            
    // //TODO can ready contain actual owner??? validate
    // this.claim(person);
    // this.addOperationComment(Operations.START, person);
    // this.setStatus(Status.IN_PROGRESS);
    //            
    // } else if (this.getStatus().equals(Status.RESERVED)) {
    //            
    // org.apache.commons.lang.Validate.notNull(this.getActualOwner());
    // if (this.getActualOwner().equals(person)) {
    //                
    // this.addOperationComment(Operations.START, person);
    // this.setStatus(Status.IN_PROGRESS);
    //                
    // } else {
    //                
    // throw new HTIllegalAccessException("This person is not permited to start the task. Task is RESERVED.", person.toString());
    //                
    // }
    //        
    // } else {
    //            
    // throw new HTIllegalStateException("Only READY or RESERVED tasks can be started.", this.getStatus());
    //            
    // }
    // }
    //    
    // /**
    // * Delegates the task.
    // * @see HumanTaskServices#delegateTask(Long, String, String)
    // * @param person
    // * @param delegatee
    // * @throws HTIllegalAccessException
    // * @throws HTIllegalStateException
    // * @throws HTRecipientNotAllowedException
    // */
    // public void delegate(Person person, Person delegatee) throws HTIllegalAccessException, HTIllegalStateException, HTRecipientNotAllowedException {
    //        
    // Validate.notNull(person);
    // Validate.notNull(person.getId());
    // Validate.notNull(delegatee);
    // Validate.notNull(delegatee.getId());
    //        
    // this.checkCanDelegate(person, delegatee);
    //        
    // this.addOperationComment(Operations.DELEGATE, person, delegatee);
    // this.actualOwner = delegatee;
    //        
    // if (!this.status.equals(Status.RESERVED)) {
    // this.setStatus(Status.RESERVED);
    // }
    // }
    //    
    // /**
    // * Checks if the task can be delegated. Throws exception if it can't.
    // * @param person
    // * @param delegatee
    // * @throws HTIllegalAccessException
    // * @throws HTRecipientNotAllowedException
    // * @throws HTIllegalStateException
    // */
    // public void checkCanDelegate(Person person, Person delegatee) throws HTIllegalAccessException, HTRecipientNotAllowedException, HTIllegalStateException {
    //        
    // Validate.notNull(person);
    // Validate.notNull(person.getId());
    // Validate.notNull(delegatee);
    // Validate.notNull(delegatee.getId());
    //        
    // if (!(this.potentialOwners.contains(person) || this.businessAdministrators.contains(person) || person.equals(this.actualOwner))) {
    // throw new HTIllegalAccessException("Person delegating the task is not a: potential owner, bussiness administrator, actual owner.");
    // }
    //        
    // if (!this.getPotentialOwners().contains(delegatee)) {
    // throw new HTRecipientNotAllowedException("Task can be delegated only to potential owners.");
    // }
    //        
    // if (!Arrays.asList(Status.READY, Status.RESERVED, Status.IN_PROGRESS).contains(this.status)) {
    // throw new HTIllegalStateException("Only READY, RESERVED, IN_PROGRESS tasks can ne delegated.", this.status);
    // }
    // }
    //    
    // /**
    // * Completes the task. Can be performed by actual owner only.
    // *
    // * @param person Person completing the task
    // * @param responseXml Xml message sent in response
    // * @throws HTIllegalStateException Task's current state does not allow completion.
    // * @throws HTIllegalArgumentException If no output data is set the operation fails.
    // * @throws HTIllegalAccessException Passed person is not task's actual owner.
    // */
    // public void complete(Person person, String responseXml) throws HTIllegalStateException, HTIllegalArgumentException, HTIllegalAccessException {
    //
    // if (responseXml == null) {
    // throw new HTIllegalArgumentException("Task must be completed with a response.");
    // }
    //	
    // if (!person.equals(this.actualOwner)) {
    // throw new HTIllegalAccessException("Task can be completed only by actual owner.");
    // }
    //
    // this.addOperationComment(Operations.COMPLETE, person);
    //	
    // this.setStatus(Status.COMPLETED);
    //	
    // //presentation parameters can depend on output message
    // this.recalculatePresentationParameters();
    // }
    //    
    // /**
    // * Fails the task. Actual owner completes the execution of the task raising a fault. Method
    // * updates task's status and fault information.
    // *
    // * @param person
    // * @param fault
    // * @throws HTIllegalAccessException
    // * @throws HTIllegalStateException
    // */
    // public void fail(Person person, Fault fault) throws HTIllegalAccessException, HTIllegalStateException {
    //	
    // if (!person.equals(this.actualOwner)) {
    // throw new HTIllegalAccessException("Task can be failed only by actual owner.");
    // }
    //	
    // //TODO check if task interface defines fault
    //	
    // this.fault = fault;
    //
    // this.addOperationComment(Operations.FAIL, person);
    //	
    // this.setStatus(Status.FAILED);
    // }
    //    
    // /**
    // * Changes tasks priority. Must be actual owner or business administrator.
    // * @param person
    // * @param priority
    // * @throws HTIllegalAccessException
    // */
    // public void changePriority(Person person, int priority) throws HTIllegalAccessException {
    //	
    // if (!person.equals(this.actualOwner) && !this.businessAdministrators.contains(person)) {
    // throw new HTIllegalAccessException("Task priority can be changed by actual owner or business administrator only.");
    // }
    //	
    // this.setPriority(priority);
    //	
    // //TODO log operation?
    // }
    //
    //
    // /***************************************************************
    // * Getters & Setters *
    // ***************************************************************/
    //
    // public void setId(Long id) {
    // this.id = id;
    // }
    //
    // public Long getId() {
    // return this.id;
    // }
    //
    // public Status getStatus() {
    // return this.status;
    // }
    //
    // public List<Attachment> getAttachments() {
    // return this.attachments;
    // }
    //
    // public Date getSuspentionTime() {
    // return (this.suspensionTime == null) ? null : (Date) this.suspensionTime.clone();
    // }
    //
    // public Assignee getActualOwner() {
    // return this.actualOwner;
    // }
    //
    // public Integer getPriority() {
    // return this.priority;
    // }
    //
    // public void setPriority(Integer priority) {
    // this.priority = priority;
    // }
    //
    // public String getCreatedBy() {
    // return this.createdBy;
    // }
    //
    // public Date getActivationTime() {
    // return this.activationTime;
    // }
    //
    // public Date getExpirationTime() {
    // return (this.expirationTime == null) ? null : (Date) this.expirationTime.clone();
    // }
    //
    // public boolean isSkippable() {
    // return this.skippable;
    // }
    //
    // public boolean isEscalated() {
    // return this.escalated;
    // }
    //
    // public QName getTaskDefinitionKey() {
    // return QName.valueOf(this.taskDefinitionKey);
    // }
    //
    // public Set<Assignee> getPotentialOwners() {
    // return this.potentialOwners;
    // }
    //
    // public Set<Assignee> getExcludedOwners() {
    // return this.excludedOwners;
    // }
    //
    // public Set<Assignee> getTaskStakeholders() {
    // return this.taskStakeholders;
    // }
    //
    // public Set<Assignee> getBusinessAdministrators() {
    // return this.businessAdministrators;
    // }
    //
    // public Set<Assignee> getNotificationRecipients() {
    // return this.notificationRecipients;
    // }
    //
    // public Date getCreatedOn() {
    // return this.createdOn == null ? null : (Date)this.createdOn.clone();
    // }
    //
    // public Map<String, Message> getInput() {
    // return this.input;
    // }
    //    
    // public Map<String, Message> getOutput() {
    // return this.output;
    // }
    //    
    // public void setFault(Fault fault) {
    // this.fault = fault;
    // }
    //
    // public Fault getFault() {
    // return this.fault;
    // }
    //    
    // /***************************************************************
    // * Infrastructure methods. *
    // ***************************************************************/
    //
    // /**
    // * Adds a comment related to operation taking place.
    // * @param operation performed operation
    // * @param people people involved, starting with person invoking the operation
    // */
    // public void addOperationComment(Operations operation, Person ... people) {
    //        
    // Validate.notNull(operation);
    //       
    // String content = null;
    //        
    // switch (operation) {
    // case CREATE:
    // content = "Created.";
    // break;
    // case START:
    // content = "Started by " + people[0];
    // break;
    // case CLAIM:
    // content = "Claimed by " + people[0];
    // break;
    // case DELEGATE:
    // content = "Delegated by " + people[0] + " to " + people[1];
    // break;
    // case NOMINATE:
    // content = "Nominated to " + people[0];
    // break;
    // case RELEASE:
    // content = "Released by " + people[0];
    // break;
    // case COMPLETE:
    // content = "Completed by " + people[0];
    // break;
    // case FAIL:
    // content = "Failed by " + people[0];
    // break;
    // default:
    // break;
    // }
    //        
    // if (content != null) {
    // this.comments.add(new Comment(content, this));
    // }
    // }
    //
    // /**
    // * Adds a comment related to operation taking place.
    // * @param operation performed operation
    // * @param people people involved, starting with person invoking the operation
    // */
    // public void addOperationComment(Operations operation, Status status) {
    //        
    // Validate.notNull(operation);
    //       
    // String content = null;
    //        
    // switch (operation) {
    // case STATUS:
    // content = "Status changed to " + status;
    // break;
    // default:
    // break;
    // }
    //        
    // if (content != null) {
    // this.comments.add(new Comment(content, this));
    // }
    // }
    //    
    //    
    // private class TaskInnerXmlUtils extends TaskXmlUtils {
    //
    // public TaskInnerXmlUtils(NamespaceContext namespaceContext) {
    // super(namespaceContext, input, output);
    // }
    // }
    //    
    // /**
    // * Evaluates xpath using task's input and output messages.
    // * @param xPathString XPath expression.
    // * @param returnType Return type.
    // * @return evaluated value
    // */
    // public Object evaluateXPath(String xPathString, QName returnType) {
    // return new TaskInnerXmlUtils(new TaskNamespaceContext()).evaluateXPath(xPathString, returnType);
    // }

    //
    // /**
    // * {@inheritDoc}
    // */
    // private class TaskNamespaceContext implements NamespaceContext {
    //
    // /**
    // * {@inheritDoc}
    // */
    // public String getNamespaceURI(String prefix) {
    //
    // if (prefix == null) {
    //                
    // throw new NullPointerException("Null prefix");
    //                
    // } else if ("htd".equals(prefix)) {
    //                
    // return "http://www.example.org/WS-HT";
    //                
    // } else if ("xml".equals(prefix)) {
    //                
    // return XMLConstants.XML_NS_URI;
    //                
    // } else {
    //                
    // String namespaceURI = getTaskDefinition().getNamespaceURI(prefix);
    // if (namespaceURI != null) {
    // return namespaceURI;
    // }
    // }
    //            
    // return XMLConstants.NULL_NS_URI;
    // }
    //        
    // /**
    // * {@inheritDoc}
    // */
    // public String getPrefix(String namespaceURI) {
    // // TODO ???
    // throw new NullPointerException("???");
    // }
    //
    // /**
    // * {@inheritDoc}
    // */
    // public Iterator getPrefixes(String namespaceURI) {
    // // ???
    // throw new NullPointerException("???");
    // }
    // }

    // /**
    // * Returns presentation parameter values.
    // * @return the presentation parameter values
    // */
    // public Map<String, Object> getPresentationParameterValues() {
    // Map<String, Object> result = new HashMap<String, Object>();
    // for (Map.Entry<String, PresentationParameter> pp : taskDto.getPresentationParameters().entrySet()) {
    // result.put(pp.getKey(), pp.getValue().getValue());
    // }
    // return result;
    // }


    public void remove() {
        Validate.isTrue(taskDto.isNotification());
        hiseEngine.getHiseDao().remove(taskDto);
    }

    public Node getInput(String part) {
        return DOMUtils.parse(taskDto.getInput().get(part).getMessage()).getDocumentElement();
    }

    public Node getOutput(String part) {
        return DOMUtils.parse(taskDto.getOutput().get(part).getMessage()).getDocumentElement();
    }
}
