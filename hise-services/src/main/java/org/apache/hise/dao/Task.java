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

package org.apache.hise.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import net.sf.saxon.Configuration;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Configurable;


/**
 * Holds task instance information. Provides task business operations.
 *
 * @author Kamil Eisenbart
 * @author Witek Wołejszo
 * @author Mateusz Lipczyński
 * @author Warren Crossing
 */
@Entity
@Table(name = "TASK")
@Configurable(preConstruction = true)
public class Task extends JpaBase {

    private final Log log = LogFactory.getLog(Task.class);
    
    @Column(nullable = false)
    private String taskDefinitionKey;

    public static enum TaskTypes {
        ALL, TASKS, NOTIFICATIONS;
    }

    /**
     * Task statuses.
     */
    public static enum Status {

        /**
         * Upon creation. Remains CREATED if there are no potential owners.
         */
        CREATED, 

        /**
         * Created task with multiple potential owners.
         */
        READY, 
        
        /**
         * Created task with single potential owner. Work started. Actual owner set.
         */
        RESERVED, 

        /**
         * Work started and task is being worked on now. Actual owner set.
         */
        IN_PROGRESS,

        /**
         * In any of its active states (Ready, Reserved, InProgress), a task can be suspended, 
         * transitioning it into the Suspended state. On resumption of the task, it transitions 
         * back to the original state from which it had been suspended.
         */
        SUSPENDED, 

        /**
         * Successful completion of the work. One of the final states.
         */
        COMPLETED, 

        /**
         * Unsuccessful completion of the work. One of the final states.
         */
        FAILED, 

        /**
         * Unrecoverable error in human task processing. One of the final states.
         */
        ERROR,

        /**
         * TODO javadoc, One of the final states.
         */
        EXITED, 
        
        /**
         * Task is no longer needed - skipped. This is considered a “good” outcome of a task. One of the final states.
         */
        OBSOLETE;

        public String value() {
            return name();
        }

        public static Status fromValue(String v) {
            return valueOf(v);
        }

    }

    /**
     * Task operations. Enumeration used to trigger comments.
     */
//    public static enum Operations {
//        CREATE, STATUS, NOMINATE, CLAIM, START, DELEGATE, RELEASE, COMPLETE, FAIL; 
//    }

    /**
     * Task's id. Autogenerated.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    /**
     * Task input message map. Maps message part to message. If
     * document style Web HumanTaskServicesImpl are used to start Task, part name
     * should be set to {@link Message.DEFAULT_PART_NAME_KEY}.
     */
    @OneToMany(cascade = { CascadeType.ALL })
    @MapKey(name = "partName")
    @JoinTable(name = "TASK_MSG_INPUT")
    private Map<String, Message> input = new HashMap<String, Message>();

    /**
     * Task output message map. Maps message part to message.
     */
    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @MapKey(name = "partName")
    @JoinTable(name = "TASK_MSG_OUTPUT")
    private Map<String, Message> output = new HashMap<String, Message>();

    /**
     * Task status.
     */
    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    @Index(name = "task_status_idx")
    private Status status;

    /**
     * Previous status of SUSPENDED Task. When Task is resumed this
     * status is copied to the status field.
     */
    @Enumerated(EnumType.STRING)
    private Status statusBeforeSuspend;

    private String actualOwner;

    /**
     * This element is used to specify the priority of the task. It is an optional element which value is an integer expression. If not present, the priority of
     * the task is unspecified. 0 is the highest priority, larger numbers identify lower priorities.
     */
    private int priority;

    /**
     * Task initiator. Depending on how the task has been instantiated the task initiator may or may not be defined.
     */
    private String createdBy;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdOn;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date activationTime;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date expirationTime;

    @OneToOne
    private Job suspendUntil;

    private boolean skippable;

    private boolean escalated;
    
    private boolean notification = false;

    public boolean isNotification() {
        return notification;
    }

    @OneToMany(mappedBy="task", cascade = {CascadeType.ALL})
    private Set<TaskOrgEntity> peopleAssignments;

//    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
//    @JoinTable(name = "TASK_EXCLUDED_OWNERS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
//    private Set<TaskOrgEntity> excludedOwners;
//
//    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
//    @JoinTable(name = "TASK_STAKEHOLDERS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
//    private Set<TaskOrgEntity> taskStakeholders;
//
//    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
//    @JoinTable(name = "TASK_BUSINESS_AMINISTRATORS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
//    private Set<TaskOrgEntity> businessAdministrators;
//
//    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
//    @JoinTable(name = "TASK_NOTIFICATION_RECIPIENTS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
//    private Set<TaskOrgEntity> notificationRecipients;

    @OneToMany(mappedBy = "task", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private List<Comment> comments = new ArrayList<Comment>();

    @OneToMany(mappedBy = "task", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private List<Attachment> attachments = new ArrayList<Attachment>();

//    @OneToMany(mappedBy = "task", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
//    private List<Deadline> deadlines = new ArrayList<Deadline>();
    
    @OneToMany(cascade = {CascadeType.ALL})
    @JoinTable(name="DEADLINE")
    private Set<Job> deadlines = new HashSet<Job>();

    /**
     * Fault information. Set when task fail method is called.
     */
    private Fault fault;
    
    /**
     * Task presentation parameters recalculated on input message change. 
     * Maps presentation parameter name to its value. Can be used as a where clause parameter
     * in task query operations.
     */
    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, mappedBy = "task", fetch = FetchType.EAGER)
    @MapKey(name = "name")
    private Map<String, PresentationParameter> presentationParameters = new HashMap<String, PresentationParameter>();


//    public List<Deadline> getDeadlines() {
//        return deadlines;
//    }
//
//    public void setDeadlines(List<Deadline> deadlines) {
//        this.deadlines = deadlines;
//    }

    public Set<Job> getDeadlines() {
        return deadlines;
    }

    public void setDeadlines(Set<Job> deadlines) {
        this.deadlines = deadlines;
    }
    
    public String getTaskDefinitionKey() {
        return taskDefinitionKey;
    }

    public void setTaskDefinitionKey(String taskDefinitionKey) {
        this.taskDefinitionKey = taskDefinitionKey;
    }
    
    public QName getTaskDefinitionName() {
        return QName.valueOf(getTaskDefinitionKey());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, Message> getInput() {
        return input;
    }

    public void setInput(Map<String, Message> input) {
        this.input = input;
    }

    public Map<String, Message> getOutput() {
        return output;
    }

    public void setOutput(Map<String, Message> output) {
        this.output = output;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatusBeforeSuspend() {
        return statusBeforeSuspend;
    }

    public void setStatusBeforeSuspend(Status statusBeforeSuspend) {
        this.statusBeforeSuspend = statusBeforeSuspend;
    }
    
    public String getActualOwner() {
        return actualOwner;
    }

    public void setActualOwner(String actualOwner) {
        this.actualOwner = actualOwner;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getActivationTime() {
        return activationTime;
    }

    public void setActivationTime(Date activationTime) {
        this.activationTime = activationTime;
    }

    public Date getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }


    public boolean isSkippable() {
        return skippable;
    }

    public void setSkippable(boolean skippable) {
        this.skippable = skippable;
    }

    public boolean isEscalated() {
        return escalated;
    }

    public void setEscalated(boolean escalated) {
        this.escalated = escalated;
    }

    

//    public Set<TaskOrgEntity> getExcludedOwners() {
//        return excludedOwners;
//    }
//
//    public void setExcludedOwners(Set<TaskOrgEntity> excludedOwners) {
//        this.excludedOwners = excludedOwners;
//    }
//
//    public Set<TaskOrgEntity> getTaskStakeholders() {
//        return taskStakeholders;
//    }
//
//    public void setTaskStakeholders(Set<TaskOrgEntity> taskStakeholders) {
//        this.taskStakeholders = taskStakeholders;
//    }
//
//    public Set<TaskOrgEntity> getBusinessAdministrators() {
//        return businessAdministrators;
//    }
//
//    public void setBusinessAdministrators(Set<TaskOrgEntity> businessAdministrators) {
//        this.businessAdministrators = businessAdministrators;
//    }
//
//    public Set<TaskOrgEntity> getNotificationRecipients() {
//        return notificationRecipients;
//    }
//
//    public void setNotificationRecipients(Set<TaskOrgEntity> notificationRecipients) {
//        this.notificationRecipients = notificationRecipients;
//    }

    public Set<TaskOrgEntity> getPeopleAssignments() {
        return peopleAssignments;
    }

    public void setPeopleAssignments(Set<TaskOrgEntity> peopleAssignments) {
        this.peopleAssignments = peopleAssignments;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public Fault getFault() {
        return fault;
    }

    public void setFault(Fault fault) {
        this.fault = fault;
    }

    public Map<String, PresentationParameter> getPresentationParameters() {
        return presentationParameters;
    }

    public void setPresentationParameters(Map<String, PresentationParameter> presentationParameters) {
        this.presentationParameters = presentationParameters;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    
    public Job getSuspendUntil() {
        return suspendUntil;
    }

    public void setSuspendUntil(Job suspendUntil) {
        this.suspendUntil = suspendUntil;
    }

    @Override
    public Object[] getKeys() {
        return new Object[] { id };
    }
}
