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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
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
import org.apache.hise.api.HumanInteractionsManager;
import org.apache.hise.api.HumanTaskServices;
import org.apache.hise.engine.TaskXmlUtils;
import org.apache.hise.lang.TaskDefinition;
import org.apache.hise.lang.faults.HTConfigurationException;
import org.apache.hise.lang.faults.HTException;
import org.apache.hise.lang.faults.HTIllegalAccessException;
import org.apache.hise.lang.faults.HTIllegalArgumentException;
import org.apache.hise.lang.faults.HTIllegalStateException;
import org.apache.hise.lang.faults.HTRecipientNotAllowedException;
import org.apache.hise.lang.xsd.htd.TExpression;
import org.apache.hise.utils.XmlUtils;
import org.hibernate.annotations.Index;
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
public class Task extends Base {

    /**
     * Logger.
     */
    @Transient
    private final Log __log = LogFactory.getLog(Task.class);
    
    @Transient
    private HumanInteractionsManager humanInteractionsManager;
    
    @Transient
    private AssigneeDao assigneeDao;

    /**
     * Key {@link Task} definition is looked up in {@link HumanInteractionsManager} by.
     */
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
    private static enum Operations {
        CREATE, STATUS, NOMINATE, CLAIM, START, DELEGATE, RELEASE, COMPLETE, FAIL; 
    }

    /**
     * Task's id. Autogenerated.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "task_seq")
    @SequenceGenerator(name = "task_seq", sequenceName = "task_seq")
    private Long id;

    /**
     * Task input message map. Maps message part to message. If
     * document style Web HumanTaskServicesImpl are used to start Task, part name
     * should be set to {@link Message.DEFAULT_PART_NAME_KEY}.
     */
    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
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
    @Column(nullable = false)
    @Index(name = "task_status_idx")
    private Status status;

    /**
     * Previous status of SUSPENDED Task. When Task is resumed this
     * status is copied to the status field.
     */
    @Enumerated(EnumType.STRING)
    private Status statusBeforeSuspend;

    /**
     * People assigned to different generic human roles.
     */
    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private Person actualOwner;

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

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date suspensionTime;

    private boolean skippable;

    private boolean escalated;
    
    private boolean notification = false;

    public boolean isNotification() {
        return notification;
    }

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "TASK_POTENTIAL_OWNERS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
    private Set<Assignee> potentialOwners;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "TASK_EXCLUDED_OWNERS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
    private Set<Assignee> excludedOwners;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "TASK_STAKEHOLDERS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
    private Set<Assignee> taskStakeholders;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "TASK_BUSINESS_AMINISTRATORS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
    private Set<Assignee> businessAdministrators;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "TASK_NOTIFICATION_RECIPIENTS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
    private Set<Assignee> notificationRecipients;

    @OneToMany(mappedBy = "task", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private List<Comment> comments = new ArrayList<Comment>();

    @OneToMany(mappedBy = "task", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private List<Attachment> attachments = new ArrayList<Attachment>();

    @OneToMany(mappedBy = "task", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private List<Deadline> deadlines = new ArrayList<Deadline>();

    /**
     * Fault information. Set when task fail method is called.
     */
    private Fault fault;
    
    @Transient
    private List<TaskStateListener> taskStateListeners;
    
    /**
     * Task presentation parameters recalculated on input message change. 
     * Maps presentation parameter name to its value. Can be used as a where clause parameter
     * in task query operations.
     */
    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, mappedBy = "task", fetch = FetchType.EAGER)
    @MapKey(name = "name")
    private Map<String, PresentationParameter> presentationParameters = new HashMap<String, PresentationParameter>();

    public void init() {
        Validate.notNull(assigneeDao);
        Validate.notNull(humanInteractionsManager);
        
        taskStateListeners = new ArrayList<TaskStateListener>();
        taskStateListeners.add(new DeadlineController());
    }
    
    
    
    public List<Deadline> getDeadlines() {
        return deadlines;
    }

    public void setDeadlines(List<Deadline> deadlines) {
        this.deadlines = deadlines;
    }



    /**
     * Task constructor.
     *
     * @param taskDefinition  task definition as an object
     * @param createdBy       person who created the task, can be null
     * @param requestXml      input data as XML string
     * @throws HTException
     */
    public void init(TaskDefinition taskDefinition, String createdBy, String requestXml) throws HTException {

        Validate.notNull(taskDefinition);
        init();
        
        this.taskDefinitionKey = taskDefinition.getTaskName().toString();
        
        Message m = new Message(requestXml);
        this.getInput().put(m.getRootNodeName(), m);

        this.recalculatePresentationParameters();
        
        //retrieveExistingAssignees check if group or people with the same name exist
        //and retrieves existing entities
        this.potentialOwners        = this.assigneeDao.saveNotExistingAssignees(taskDefinition.evaluateHumanRoleAssignees(GenericHumanRole.POTENTIAL_OWNERS,          this));
        this.businessAdministrators = this.assigneeDao.saveNotExistingAssignees(taskDefinition.evaluateHumanRoleAssignees(GenericHumanRole.BUSINESS_ADMINISTRATORS,   this));
        this.excludedOwners         = this.assigneeDao.saveNotExistingAssignees(taskDefinition.evaluateHumanRoleAssignees(GenericHumanRole.EXCLUDED_OWNERS,           this));
        this.notificationRecipients = this.assigneeDao.saveNotExistingAssignees(taskDefinition.evaluateHumanRoleAssignees(GenericHumanRole.NOTIFICATION_RECIPIENTS,   this));
        this.taskStakeholders       = this.assigneeDao.saveNotExistingAssignees(taskDefinition.evaluateHumanRoleAssignees(GenericHumanRole.TASK_STAKEHOLDERS,         this));
        
        //TODO test
        if (this.taskStakeholders.isEmpty()) {
            __log.error("No task stakeholders. Wrong configuration. Cannot create task.");
            throw new HTConfigurationException("No task stakeholders. Wrong configuration. Cannot create task.", null);
        }

        this.addOperationComment(Operations.CREATE);

        this.createdBy = createdBy;
        this.createdOn = new Date();
        this.activationTime = new Date();
        this.escalated = false;
        this.notification = false;
        this.setStatus(Status.CREATED);
       
        Person nominatedPerson = this.nominateActualOwner(this.potentialOwners);
        if (nominatedPerson != null) {
            
            this.actualOwner = nominatedPerson;
            this.addOperationComment(Operations.NOMINATE, nominatedPerson);
            this.setStatus(Status.RESERVED);
            
        } else if (!this.potentialOwners.isEmpty()) {
            
            this.setStatus(Status.READY);
            
        }
        
        recalculatePresentationParameters();
        
        recalculatePriority();
    }

//    @PostLoad
//    public void postLoad() {
//        log.info("Post load.");
//    }
//    
//    @PrePersist
//    public void prePersist() {
//        log.info("Pre persist.");
//    }

    /**
     * If there is only one person in the given list, it
     * returns this person. Otherwise, it returns null.
     *
     * @param assignees    list of assignees that can contain persons and groups
     * @return             the only person in the list, otherwise null
     */
    protected final Person nominateActualOwner(Set<Assignee> assignees) {

        Validate.notNull(assignees);

        Person result = null;
        int count = 0;
        for (Assignee assignee : assignees) {
            if (assignee instanceof Person) {
                if (count++ > 0) {
                    break;
                }
                result = (Person)assignee;
            }
        }

        return (count == 1) ? result : null;
    }
    
    /**
     * Returns task's name in a required language.
     *
     * @param lang subject language according ISO, e.g. en-US, pl, de-DE
     * @return name
     */
    public String getName(String lang) {
        Validate.notNull(lang);
        return this.getTaskDefinition().getName(lang);
    }
    
    public QName getTaskName() {
        return this.getTaskDefinition().getTaskName();
    }

    /**
     * Returns a formatted task subject in a required language.
     *
     * @param lang subject language according ISO, e.g. en-US, pl, de-DE
     * @return subject
     */
    public String getSubject(String lang) {
        Validate.notNull(lang);
        return this.getTaskDefinition().getSubject(lang, this);
    }

    /**
     * Returns a formatted task description in a required language and form.
     *
     * @param lang The description language according ISO, e.g. en-US, pl, de-DE
     * @param contentType The content type, text/plain for plain text or text/html for HTML-formatted text.
     * @return description
     */
    public String getDescription(String lang, String contentType) {
        Validate.notNull(lang);
        Validate.notNull(contentType);
        return this.getTaskDefinition().getDescription(lang, contentType, this);
    }

    /**
     * Recalculates priority. To be called after object creation or input message update.
     */
    private void recalculatePriority() {
        __log.info("Recalculating priority.");
        this.priority = this.getTaskDefinition().getPriority(this);
    }
    
    /**
     * Recalculates presentation parameter values. To be called after object creation or
     * input message update.
     */
    private void recalculatePresentationParameters() {

        __log.info("Recalculating presentation parameters.");

        Map<String, Object> pp = this.getTaskDefinition().getTaskPresentationParameters(this);

        //replace all calculated
        for (Entry<String, Object> entry : pp.entrySet()) {
            
            PresentationParameter p = this.presentationParameters.get(entry.getKey());
            
            if (p != null) {
                
                p.setValue(entry.getValue() == null ? null : entry.getValue());

            } else {
                
                p = new PresentationParameter();
                p.setTask(this);
                p.setName(entry.getKey());
                //TODO test
                p.setValue(entry.getValue() == null ? null : entry.getValue());

                this.presentationParameters.put(p.getName(), p);
            }
        }
        
        //remove obsolete from presentationParameters
        Set<String> allKeys = this.presentationParameters.keySet();
        for (String key : allKeys) {
            if (!pp.containsKey(key)) {
                allKeys.remove(key);
            }
        }
    }

    /**
     * Adds an attachment to the task.
     *
     * @param attachment    a new attachment to add
     */
    public void addAttachment(Attachment attachment) {
        Validate.notNull(attachment);
        this.attachments.add(attachment);
        //TODO addComment
    }

    /***************************************************************
     * Task operations                                             *
     ***************************************************************/
    
    /**
     * Sets Task status. Task status is changed indirectly by operations on tasks. Status
     * change operation comment is added. If new status is SUSPENDED and transition is
     * valid, current status is remembered in {@link Task#statusBeforeSuspend}.
     *
     * @param status        The new {@link Status}.
     * @throws HTIllegalStateException thrown when impossible transition is forced by the caller. 
     */
    private void setStatus(Status status) throws HTIllegalStateException {
        
        Validate.notNull(status);

        boolean isOk = false;

        // check if change is valid for current state
        if (this.status != null) {

            switch (this.status) {

            case CREATED:
                if (status == Status.READY || status == Status.RESERVED) {
                    isOk = true;
                }
                
                break;

            case READY:
                if (status == Status.RESERVED || status == Status.IN_PROGRESS ||
                    status == Status.READY || status == Status.SUSPENDED) {
                    isOk = true;
                }

                break;

            case RESERVED:
                if (status == Status.IN_PROGRESS || status == Status.READY || 
                    status == Status.SUSPENDED   || status == Status.RESERVED) {
                    isOk = true;
                }

                break;

            case IN_PROGRESS:
                if (status == Status.COMPLETED || status == Status.FAILED || 
                    status == Status.RESERVED  || status == Status.READY || 
                    status == Status.SUSPENDED) {
                    isOk = true;
                }

                break;

            default:
                break;

            }

            if (isOk) {

                __log.debug("Changing Task status : " + this + " status from: " + this.status + " to: " + status);

                if (status.equals(Status.SUSPENDED)) {
                    this.statusBeforeSuspend = this.status;
                } else {
                    Validate.isTrue(this.statusBeforeSuspend == null);
                }

                this.addOperationComment(Operations.STATUS, status);
                Status oldStatus = this.status;
                this.status = status;

                for (TaskStateListener l : taskStateListeners) {
                    l.stateChanged(this, oldStatus, status);
                }

            } else {

                __log.error("Changing Task status: " + this + " status from: " + this.status + " to: " + status + " is not allowed.");
                throw new org.apache.hise.lang.faults.HTIllegalStateException("Changing Task's: " + this + " status from: " + this.status + " to: " + status
                        + " is not allowed, or task is SUSPENDED", status);
            }

        } else {

            //TODO check allowed first statuses
            __log.debug("Changing Task status: " + this + " status from: NULL to: " + status);
            this.addOperationComment(Operations.STATUS, status);
            this.status = status;

            for (TaskStateListener l : taskStateListeners) {
                l.stateChanged(this, null, status);
            }
        }
    }
    
    /**
     * Claims task. Task in READY status can be claimed by
     * people from potential owners group not listed in excluded owners.
     * 
     * @param person                    The Person that claims the task.
     * @throws HTIllegalStateException  Thrown when task is in illegal state for claim i.e. not READY. 
     * @throws HTIllegalAccessException Thrown when task is in illegal state for claim i.e. not READY or person cannot
     *                                  become actual owner i.e. not potential owner or excluded.
     */
    public void claim(Person person) throws HTIllegalStateException, HTIllegalAccessException {
        
        Validate.notNull(person);
        Validate.notNull(person.getId());

        this.checkCanClaim(person);

        this.actualOwner = person;
        this.addOperationComment(Operations.CLAIM, person);
        this.setStatus(Task.Status.RESERVED);
    }

    /**
     * Checks if the task can be claimed by the person. Throws exception if it is not. 
     * @param person
     * @return
     * @throws HTIllegalStateException 
     * @throws HTIllegalAccessException 
     */
    public void checkCanClaim(Person person) throws HTIllegalStateException, HTIllegalAccessException {
        
        Validate.notNull(person);
        Validate.notNull(person.getId());
        
        //actual owner set
        if (this.getActualOwner() != null) {
            throw new HTIllegalStateException("Task not claimable. Actual owner set: " + this.getActualOwner(), this.getStatus());
        }

        //not ready
        if (!this.getStatus().equals(Task.Status.READY)) {
            throw new HTIllegalStateException("Task not claimable. Not READY.", this.getStatus());
        }
        
        // check if the task can be claimed by person
        if (!this.getPotentialOwners().contains(person)) {
            throw new HTIllegalAccessException("Not a potential owner.", person.getName());
        }

        //TODO test
        // check if the person is excluded from potential owners
        if ((this.getExcludedOwners() != null && this.getExcludedOwners().contains(person))) {
            throw new HTIllegalAccessException("Person is excluded from potential owners.", person.getName());
        }
    }
    
    /**
     * Releases the Task.
     * @param person                        The person that is releasing the Task.
     * @throws HTIllegalAccessException     The person is not authorised to perform release operation.
     * @throws HTIllegalStateException      Task cannot be released.
     * @see HumanTaskServices.releaseTask
     */
    public void release(Person person) throws HTIllegalAccessException, HTIllegalStateException {
        
        Validate.notNull(person);
        Validate.notNull(person.getId());

        //TODO test
        if (this.actualOwner == null) {
            throw new HTIllegalAccessException("Task without actual owner cannot be released.");
        }

        //TODO test
        if (!this.actualOwner.equals(person) && !this.getBusinessAdministrators().contains(person)) {
            throw new HTIllegalAccessException("Calling person is neither the task's actual owner not business administrator");
        }

        this.actualOwner = null;
        this.addOperationComment(Operations.RELEASE, person);
        this.setStatus(Status.READY);
    }
    
    /**
     * Starts task.
     * @see HumanTaskServices#startTask(Long, String)
     * @param person
     * @throws HTIllegalStateException 
     * @throws HTIllegalAccessException 
     */
    public void start(Person person) throws HTIllegalStateException, HTIllegalAccessException {
        
        Validate.notNull(person);
        Validate.notNull(person.getId());
        
        //only potential owner can start the task
        if (!this.getPotentialOwners().contains(person)) {
            throw new HTIllegalAccessException("This person is not permited to start the task.", person.toString());
        }

        //ready
        if (this.getStatus().equals(Status.READY)) {
            
            //TODO can ready contain actual owner??? validate
            this.claim(person);
            this.addOperationComment(Operations.START, person);
            this.setStatus(Status.IN_PROGRESS);
            
        } else if (this.getStatus().equals(Status.RESERVED)) {
            
            org.apache.commons.lang.Validate.notNull(this.getActualOwner());
            if (this.getActualOwner().equals(person)) {
                
                this.addOperationComment(Operations.START, person);
                this.setStatus(Status.IN_PROGRESS);
                
            } else {
                
                throw new HTIllegalAccessException("This person is not permited to start the task. Task is RESERVED.", person.toString());
                
            }
        
        } else {
            
            throw new HTIllegalStateException("Only READY or RESERVED tasks can be started.", this.getStatus());
            
        }
    }
    
    /**
     * Delegates the task.
     * @see HumanTaskServices#delegateTask(Long, String, String)
     * @param person
     * @param delegatee
     * @throws HTIllegalAccessException 
     * @throws HTIllegalStateException 
     * @throws HTRecipientNotAllowedException 
     */
    public void delegate(Person person, Person delegatee) throws HTIllegalAccessException, HTIllegalStateException, HTRecipientNotAllowedException {
        
        Validate.notNull(person);
        Validate.notNull(person.getId());
        Validate.notNull(delegatee);
        Validate.notNull(delegatee.getId());
        
        this.checkCanDelegate(person, delegatee);
        
        this.addOperationComment(Operations.DELEGATE, person, delegatee);
        this.actualOwner = delegatee;
        
        if (!this.status.equals(Status.RESERVED)) {
            this.setStatus(Status.RESERVED);
        }
    }
    
    /**
     * Checks if the task can be delegated. Throws exception if it can't.
     * @param person
     * @param delegatee
     * @throws HTIllegalAccessException
     * @throws HTRecipientNotAllowedException
     * @throws HTIllegalStateException
     */
    public void checkCanDelegate(Person person, Person delegatee) throws HTIllegalAccessException, HTRecipientNotAllowedException, HTIllegalStateException {
        
        Validate.notNull(person);
        Validate.notNull(person.getId());
        Validate.notNull(delegatee);
        Validate.notNull(delegatee.getId());
        
        if (!(this.potentialOwners.contains(person) || this.businessAdministrators.contains(person) || person.equals(this.actualOwner))) {
            throw new HTIllegalAccessException("Person delegating the task is not a: potential owner, bussiness administrator, actual owner.");
        }
        
        if (!this.getPotentialOwners().contains(delegatee)) {
            throw new HTRecipientNotAllowedException("Task can be delegated only to potential owners.");
        }
        
        if (!Arrays.asList(Status.READY, Status.RESERVED, Status.IN_PROGRESS).contains(this.status)) {
            throw new HTIllegalStateException("Only READY, RESERVED, IN_PROGRESS tasks can ne delegated.", this.status);
        } 
    }
    
    /**
     * Completes the task. Can be performed by actual owner only.
     *
     * @param person		Person completing the task
     * @param responseXml	Xml message sent in response
     * @throws HTIllegalStateException		Task's current state does not allow completion.
     * @throws HTIllegalArgumentException	If no output data is set the operation fails.
     * @throws HTIllegalAccessException		Passed person is not task's actual owner.
     */
    public void complete(Person person, String responseXml) throws HTIllegalStateException, HTIllegalArgumentException, HTIllegalAccessException {

	if (responseXml == null) {
	    throw new HTIllegalArgumentException("Task must be completed with a response.");
	}
	
	if (!person.equals(this.actualOwner)) {
	    throw new HTIllegalAccessException("Task can be completed only by actual owner.");
	}

	this.addOperationComment(Operations.COMPLETE, person);
	
	this.setStatus(Status.COMPLETED);
	
	//presentation parameters can depend on output message
	this.recalculatePresentationParameters();
    }
    
    /**
     * Fails the task. Actual owner completes the execution of the task raising a fault. Method
     * updates task's status and fault information. 
     *
     * @param person
     * @param fault
     * @throws HTIllegalAccessException 
     * @throws HTIllegalStateException 
     */
    public void fail(Person person, Fault fault) throws HTIllegalAccessException, HTIllegalStateException {
	
	if (!person.equals(this.actualOwner)) {
	    throw new HTIllegalAccessException("Task can be failed only by actual owner.");
	}
	
	//TODO check if task interface defines fault
	
	this.fault = fault;

	this.addOperationComment(Operations.FAIL, person);
	
	this.setStatus(Status.FAILED);
    }
    
    /**
     * Changes tasks priority. Must be actual owner or business administrator.
     * @param person
     * @param priority
     * @throws HTIllegalAccessException 
     */
    public void changePriority(Person person, int priority) throws HTIllegalAccessException {
	
	if (!person.equals(this.actualOwner) && !this.businessAdministrators.contains(person)) {
	    throw new HTIllegalAccessException("Task priority can be changed by actual owner or business administrator only.");
	}
	
	this.setPriority(priority);
	
	//TODO log operation?
    }

    /**
     * Returns task definition of this task.
     * @return
     */
    public TaskDefinition getTaskDefinition() {
        
        if (this.humanInteractionsManager == null) {
            throw new HTConfigurationException("Human interactions manager not available.", null);
        }
        
        return this.humanInteractionsManager.getTaskDefinition(this.getTaskDefinitionKey());
    }

    /***************************************************************
     * Getters & Setters *
     ***************************************************************/

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public Status getStatus() {
        return this.status;
    }

    public List<Attachment> getAttachments() {
        return this.attachments;
    }

    public Date getSuspentionTime() {
        return (this.suspensionTime == null) ? null : (Date) this.suspensionTime.clone();
    }

    public Assignee getActualOwner() {
        return this.actualOwner;
    }

    public Integer getPriority() {
        return this.priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public Date getActivationTime() {
        return this.activationTime;
    }

    public Date getExpirationTime() {
        return (this.expirationTime == null) ? null : (Date) this.expirationTime.clone();
    }

    public boolean isSkippable() {
        return this.skippable;
    }

    public boolean isEscalated() {
        return this.escalated;
    }

    public QName getTaskDefinitionKey() {
        return QName.valueOf(this.taskDefinitionKey);
    }

    public Set<Assignee> getPotentialOwners() {
        return this.potentialOwners;
    }

    public Set<Assignee> getExcludedOwners() {
        return this.excludedOwners;
    }

    public Set<Assignee> getTaskStakeholders() {
        return this.taskStakeholders;
    }

    public Set<Assignee> getBusinessAdministrators() {
        return this.businessAdministrators;
    }

    public Set<Assignee> getNotificationRecipients() {
        return this.notificationRecipients;
    }

    public Date getCreatedOn() {
        return this.createdOn == null ? null : (Date)this.createdOn.clone();
    }

    public Map<String, Message> getInput() {
        return this.input;
    }
    
    public Map<String, Message> getOutput() {
        return this.output;
    }
    
    public void setFault(Fault fault) {
	this.fault = fault;
    }

    public Fault getFault() {
	return this.fault;
    }
    
    /***************************************************************
     * Infrastructure methods.                                     *
     ***************************************************************/

    /**
     * Adds a comment related to operation taking place.
     * @param operation     performed operation
     * @param people        people involved, starting with person invoking the operation
     */
    public void addOperationComment(Operations operation, Person ... people) {
        
        Validate.notNull(operation);
       
        String content = null;
        
        switch (operation) {
        case CREATE:
            content = "Created.";
            break;
        case START:
            content = "Started by " + people[0];
            break;
        case CLAIM:            
            content = "Claimed by " + people[0];
            break;
        case DELEGATE:            
            content = "Delegated by " + people[0] + " to " + people[1];
            break;
        case NOMINATE:            
            content = "Nominated to " + people[0];
            break;
        case RELEASE:            
            content = "Released by " + people[0];
            break;
        case COMPLETE:            
            content = "Completed by " + people[0];
            break;
        case FAIL:            
            content = "Failed by " + people[0];
            break;
        default:
            break;
        }
        
        if (content != null) {
            this.comments.add(new Comment(content, this));
        }
    }

    /**
     * Adds a comment related to operation taking place.
     * @param operation     performed operation
     * @param people        people involved, starting with person invoking the operation
     */
    public void addOperationComment(Operations operation, Status status) {
        
        Validate.notNull(operation);
       
        String content = null;
        
        switch (operation) {
        case STATUS:
            content = "Status changed to " + status;
            break;
        default:
            break;
        }
        
        if (content != null) {
            this.comments.add(new Comment(content, this));
        }
    }
    
    /**
     * Returns presentation parameter values.
     * @return the presentation parameter values
     */
    public Map<String, Object> getPresentationParameterValues() {
        Map<String, Object> result = new HashMap<String, Object>();
        for (Map.Entry<String, PresentationParameter> pp : this.presentationParameters.entrySet()) {
            result.put(pp.getKey(), pp.getValue().getValue());
        }
        return result;
    }
    
    private class TaskInnerXmlUtils extends TaskXmlUtils {

        public TaskInnerXmlUtils(NamespaceContext namespaceContext) {
            super(namespaceContext, input, output);
        }
    }
    
    /**
     * Evaluates xpath using task's input and output messages.
     * @param xPathString   XPath expression.
     * @param returnType    Return type.
     * @return evaluated value
     */
    public Object evaluateXPath(String xPathString, QName returnType) {
        return new TaskInnerXmlUtils(new TaskNamespaceContext()).evaluateXPath(xPathString, returnType);
    }
    
    public Object evaluateExpression(TExpression expr) {
        Configuration config = Configuration.makeConfiguration(null, null);
        StaticQueryContext sqc = new StaticQueryContext(config);
        DynamicQueryContext dqc = new DynamicQueryContext(config);
        try {
            String query = XmlUtils.getStringContent(expr.getContent());
            XQueryExpression e = sqc.compileQuery(query);
            Object value = e.evaluateSingle(dqc);
            __log.debug("result for expression " + query + " " + value + " value class " + value == null ? null : value.getClass());
            return value;
        } catch (XPathException e) {
            __log.error("", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    private class TaskNamespaceContext implements NamespaceContext {

        /**
         * {@inheritDoc}
         */
        public String getNamespaceURI(String prefix) {

            if (prefix == null) {
                
                throw new NullPointerException("Null prefix");
                
            } else if ("htd".equals(prefix)) {
                
                return "http://www.example.org/WS-HT";
                
            } else if ("xml".equals(prefix)) {
                
                return XMLConstants.XML_NS_URI;
                
            } else {
                
                String namespaceURI = getTaskDefinition().getNamespaceURI(prefix);
                if (namespaceURI != null) {
                    return namespaceURI;
                }
            } 
            
            return XMLConstants.NULL_NS_URI;
        }
        
        /**
         * {@inheritDoc}
         */
        public String getPrefix(String namespaceURI) {
            // TODO ???
            throw new NullPointerException("???");
        }

        /**
         * {@inheritDoc}
         */
        public Iterator getPrefixes(String namespaceURI) {
            // ???
            throw new NullPointerException("???");
        }
    }
    
    /**
     * Sets {@link AssigneeDao}. IoC container method.
     * @param assigneeDao
     */
    public void setAssigneeDao(AssigneeDao assigneeDao) {
        this.assigneeDao = assigneeDao;
    }
    
    
    
    /***************************************************************
     * Infrastructure methods.                                     *
     ***************************************************************/

    public void setHumanInteractionsManager(HumanInteractionsManager humanInteractionsManager) {
        this.humanInteractionsManager = humanInteractionsManager;
    }

    /**
     * Returns the task hashcode.
     * @return task hash code
     */
    @Override
    public int hashCode() {
        return this.id == null ? 0 : this.id.hashCode();
    }

    /**
     * Checks whether the task is equal to another object.
     * @param obj object to compare
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Task == false) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        Task rhs = (Task) obj;
        return new EqualsBuilder().append(this.id, rhs.id).isEquals();
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", this.id).append("name", this.getTaskDefinition().getTaskName()).append("actualOwner", this.getActualOwner()).toString();
    }

}
