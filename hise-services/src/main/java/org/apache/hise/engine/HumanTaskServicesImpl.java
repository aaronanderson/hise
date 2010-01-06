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
import org.apache.hise.dao.Assignee;
import org.apache.hise.dao.AssigneeDao;
import org.apache.hise.dao.Fault;
import org.apache.hise.dao.GenericHumanRole;
import org.apache.hise.dao.Person;
import org.apache.hise.dao.TaskDao;
import org.apache.hise.lang.TaskDefinition;
import org.apache.hise.lang.faults.HTException;
import org.apache.hise.lang.faults.HTIllegalAccessException;
import org.apache.hise.lang.faults.HTIllegalArgumentException;
import org.apache.hise.lang.faults.HTIllegalOperationException;
import org.apache.hise.lang.faults.HTIllegalStateException;
import org.apache.hise.lang.faults.HTRecipientNotAllowedException;
import org.apache.hise.runtime.Task;
import org.apache.hise.utils.DOMUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Element;


/**
 * Human task engine services.
 *
 * @author Kamil Eisenbart
 * @author Witek Wołejszo
 * @author Mateusz Lipczyński
 */
public class HumanTaskServicesImpl {

    private final Log log = LogFactory.getLog(HumanTaskServicesImpl.class);


    /**
     * Fully implemented methods - visible in interface.
     */

    private HISEEngine engine;

    
    public void setEngine(HISEEngine engine) {
        this.engine = engine;
    }

    public void receive(QName portType, String operation, Element body, String createdBy) throws HTException {
        QName taskName = engine.getTaskName(portType, operation);
        assert(taskName != null);
        log.debug("routed " + portType + " " + operation + " -> " + taskName);
        createTask(taskName, createdBy, DOMUtils.domToString(DOMUtils.getFirstElement(DOMUtils.getFirstElement(body))));
    }
    
    /**
     * {@inheritDoc}
     */
    public Task createTask(QName taskName, String createdBy, String requestXml) throws HTException {

        Validate.notNull(taskName);
        Validate.notNull(requestXml);
        
        log.info("Creating task: " + taskName + " , createdBy: " + createdBy);

        TaskDefinition taskDefinition = engine.getTaskDefinition(taskName);

        Task newTask = new Task();
        newTask.init(taskDefinition, createdBy, requestXml);
        return newTask;
    }

    /**
     * {@inheritDoc}
     */
    public List<Task> getMyTasks(String personName, TaskTypes taskType, GenericHumanRole genericHumanRole, String workQueue, List<Task.Status> statuses,
            String whereClause, String orderByClause, String createdOnClause, Integer maxTasks, Integer offset) throws HTException {

        if (personName == null && workQueue == null) {
            throw new org.apache.hise.lang.faults.HTIllegalArgumentException("parameter not specified","workQueue");
        }

        Person person = null;

        if (workQueue == null) {
            person = this.assigneeDao.getPerson(personName);
        }

        if (person == null && workQueue == null) {
            throw new HTIllegalAccessException("Not a valid name, no such Assignee found: " + personName);
        }

        if (taskType == null) {
            throw new org.apache.hise.lang.faults.HTIllegalArgumentException("parameter not specified","task type");
        }

        try {

            return this.taskDao.getTasks(person, taskType, genericHumanRole, workQueue, statuses,
                    whereClause, orderByClause, createdOnClause, maxTasks, offset);

        } catch (Exception x) {
            throw new HTIllegalOperationException(x.getMessage(), "getMyTasks", x);
        }
    }

    /**
     * {@inheritDoc} 
     */
    public void claimTask(Long taskId, String personName) throws HTIllegalAccessException, HTIllegalArgumentException, HTIllegalStateException {

        Validate.notNull(taskId);
        Validate.notNull(personName);
        
        Person person = this.assigneeDao.getPerson(personName);
        
        if (person == null) {
            throw new HTIllegalAccessException("Not found", personName);
        }

        Task task = locateTask(taskId);

        task.claim(person);

        this.taskDao.update(task);
    }

    /**
     * {@inheritDoc}
     */
    public void startTask(Long taskId, String personName) throws HTIllegalAccessException, HTIllegalArgumentException, HTIllegalStateException {

        Validate.notNull(taskId);
        Validate.notNull(personName);

        Person person = this.assigneeDao.getPerson(personName);

        if (null == person) {
            throw new HTIllegalAccessException("Person not found: ", personName);
        }

        Task task = locateTask(taskId);

        task.start(person);

        this.taskDao.update(task);
    }

    /**
     * {@inheritDoc}
     */
    public void releaseTask(Long taskId, final String personName) throws HTIllegalArgumentException,HTIllegalAccessException, HTIllegalStateException {

        Validate.notNull(taskId);
        Validate.notNull(personName);

        final Person person = this.assigneeDao.getPerson(personName);

        if (person == null) {
            throw new HTIllegalAccessException("Person not found: ", personName);
        }

        final Task task = locateTask(taskId);

        task.release(person);

        this.taskDao.update(task);
    }

    /**
     * {@inheritDoc}
     */
    public void delegateTask(Long taskId, String personName, String delegateeName) throws HTIllegalAccessException, HTIllegalStateException, HTIllegalArgumentException, HTRecipientNotAllowedException {

        Validate.notNull(taskId);
        Validate.notNull(personName);
        Validate.notNull(delegateeName);
        
        final Person person = this.assigneeDao.getPerson(personName);
        final Person delegatee = this.assigneeDao.getPerson(delegateeName);
        
        if (person == null) {
            throw new HTIllegalAccessException("Person not found: ", delegateeName);
        }
        
        if (delegatee == null) {
            throw new HTIllegalAccessException("Delegatee not found: ", delegateeName);
        }

        final Task task = locateTask(taskId);

        task.delegate(person, delegatee);
        
        this.taskDao.update(task);
    }
    
    /**
     * {@inheritDoc}
     */
    public void completeTask(Long taskId, String personName, String responseXml)
	    throws HTIllegalStateException, HTIllegalArgumentException, HTIllegalAccessException {

	Validate.notNull(taskId);
	Validate.notNull(personName);
        Validate.notNull(responseXml);
        
        final Person person = this.assigneeDao.getPerson(personName);
        
        if (person == null) {
            throw new HTIllegalAccessException("Person not found: ", personName);
        }
        
        final Task task = locateTask(taskId);

        task.complete(person, responseXml);
        
        this.taskDao.update(task);
    }
    
    /**
     * {@inheritDoc}
     */
    public void failTask(Long taskId, String personName, String faultName, String faultData) throws HTIllegalAccessException, HTIllegalArgumentException, HTIllegalStateException {

	Validate.notNull(taskId);
	Validate.notNull(personName);
        Validate.notNull(faultName);
        
        final Person person = this.assigneeDao.getPerson(personName);
        
        if (person == null) {
            throw new HTIllegalAccessException("Person not found: ", personName);
        }
        
        final Task task = locateTask(taskId);
        final Fault fault = new Fault();
        fault.setName(faultName);
        fault.setData(faultData);

        task.fail(person, fault);
        
        this.taskDao.update(task);
	
    }
    
    /**
     * {@inheritDoc} 
     */
    public void changeTaskPrioity(Long taskId, String personName, int priority) throws HTIllegalAccessException, HTIllegalArgumentException {

	final Person person = this.assigneeDao.getPerson(personName);
        
        if (person == null) {
            throw new HTIllegalAccessException("Person not found: ", personName);
        }
        
	Task task = locateTask(taskId);

	task.changePriority(person, priority);
	
	this.taskDao.update(task);
    }
    
    /**
     * Work in progress - visible in interface.
     */
    
    /**
     * Later.
     */

//    @Transactional(propagation = Propagation.REQUIRED)
//    // TODO How to distinguish groups and people? now works on people only
//    public Task forwardTask(Long taskId, String assigneeName) throws HumanTaskException {
//
//        Person owner = (Person) task.getActualOwner();
//
//        Assignee assignee = assigneeDao.getPerson(assigneeName);
//
//        // List<Person> list = this.;
//        // log.info(g.getPeople().size());
//
//        if (assignee == null) {
//            log.error("Task without assignee cannot be forwarded.");
//            throw new HumanTaskException("Task without assignee cannot be forwarded");
//        }
//
//        // TODO check who is forwarding the task
//        /*
//         * if ((t.getStatus()==Status.IN_PROGRESS || t.getStatus()==Status.RESERVED)&& owner==null){
//         * log.error("this person or a group cannot forward this task"); throw new HumanTaskException("this person or a group cannot forward this task" ); }
//         */
//
//        if (task.getStatus() == Status.IN_PROGRESS || task.getStatus() == Status.RESERVED) {
//            this.releaseTask(task, owner.getName());
//        }
//
//        if (!(task.getStatus() == Status.READY)) {
//
//            log.error("Task with other stus then Ready cannot be forwarded");
//            throw new HumanTaskException("Task with other stus then Ready cannot be forwarded");
//        }
//
//        // erasing actual owner from potential owners or all potential owners
//        // group
//        if (owner != null) {
//
//            this.removeAssigneeFromPotentialOwners(task, owner);
//        } else {
//
//            task.setPotentialOwners(null);
//        }
//
//        //TODO always Person
//        if (assignee instanceof Group) {
//
//            Group g = (Group) assignee;
//            List<Person> people = g.getPeople();
//            // TODO adding to potentialownersgroup new owners form the group
//            this.addPotentialOwner(task, people);
//
//        } else {
//
//            this.addPotentialOwner(task, assignee);
//
//        }
//
//        // adding new potential owners
//        // t = t.addPotentialOwner(assignee);
//
//        taskDao.update(task);
//        return task;
//    }
//
//    /**
//     * Removes person from potential owners of specified task.
//     *
//     * @param task Task to process
//     * @param person Person to remove from task owners
//     */
//    public void removeAssigneeFromPotentialOwners(Long taskId, Assignee person) throws HTIllegalArgumentException {
//        int i = 0;
//        boolean removed = false;
//        Task task = locateTask(taskId);
//        Set<Assignee> list = task.getPotentialOwners();
//        Assignee assignee;
//        while (!removed) {
//            assignee = list.get(i);
//
//            if (((Person) assignee).equals(person)) {
//                list.remove(i);
//                removed = true;
//            }
//            i++;
//        }
//    }
//
//
//    /**
//     * Stops task in progres.
//     *
//     * @param task Task to process
//     * @return Updated task
//     * @throws HTException Thrown if task is not in progress or in case of problems while updating task
//     */
//    public Task stopTaskInProgress(Long taskId) throws HTException {
//
//        Task task = locateTask(taskId);
//        
//        if (!(task.getStatus() == Status.IN_PROGRESS)) {
//            log.error("Task has to be In_Progress");
//            throw new HTException("Cannot stop task that is not in progress");
//        }
//
//        task.setStatus(Status.RESERVED);
//        taskDao.update(task);
//
//        return task;
//
//    }
//
//    /**
//     * Returns task definition object matching given key.
//     *
//     * @param key
//     * @return task definition
//     */
//    private TaskDefinition findTaskDefinitionByKey(String key) throws HumanTaskException {
//
//        return taskManager.getTaskDefinitionByKey(key);
////
////        for (TaskDefinition td : taskDefinitions) {
////            if (key.equals(td.getKey())) {
////                return td;
////            }
////        }
////
////        return null;
//    }
//
//
//
//    /**
//     * Suspends task.
//     * 
//     * @param task Task to process
//     * @param personName Name of person processing task
//     * @throws HTException Thrown if specified person doesn't have permission to process task or in case of problems while updating task
//     */
//    public void suspendTask(Long taskId, String personName) throws HTException {
//
//        final Person person = (Person) assigneeDao.getPerson(personName);
//        final Task task = locateTask(taskId);
//        if (!((task.getPotentialOwners().contains(person) && task.getStatus() == Status.READY)
//                || task.getActualOwner().equals(person) || task.getBusinessAdministrators().contains(person))) {
//            log.error("you don't have a permission to suspend the task");
//            throw new HTException("you don't have a permission to suspend the task");
//        }
//        task.setStatus(Status.SUSPENDED);
//
//        taskDao.update(task);
//    }
//
//    /**
//     * Resumes task after suspension.
//     * 
//     * @param task Task to process
//     * @param personName Name of person processing task
//     * @throws HTException Thrown if specified person doesn't have permission to process task or in case of problems while updating task
//     */
//    public void resumeTask(Long taskId, String personName) throws HTException {
//
//        final Person person = (Person) assigneeDao.getPerson(personName);
//        final Task task = locateTask(taskId);
//        
//        if (!((task.getPotentialOwners().contains(person) && task.getStatus() == Status.READY)
//                || task.getActualOwner().equals(person) || task.getBusinessAdministrators().contains(person))) {
//            log.error("you don't have a permission to resume the task");
//            throw new HTException("you don't have a permission to resume the task");
//        }
//
//        task.resume();
//        taskDao.update(task);
//    }
//
//    /**
//     * Completes task.
//     *
//     * @param task Task to process
//     * @param data ?
//     * @param personName Name of person processing task
//     * @throws HTException Thrown if specified person doesn't have permission to process task or in case of problems while updating task
//     */
//    public void completeTask(Long taskId, String data, String personName) throws HTException {
//
//        final Person person = (Person) assigneeDao.getPerson(personName);
//        final Task task = locateTask(taskId);
//        if (!task.getActualOwner().equals(person)) {
//
//            log.error("this person doesn't have a permission to complete the task");
//            throw new HTException("this person doesn't have a permission to complete the task");
//        }
//
//        if (data == null) {
//            log.error("data cannot be empty");
//            throw new IllegalArgumentException("data cannot be empty");
//        }
//
//        task.setStatus(Status.COMPLETED);
//
//        taskDao.update(task);
//
//    }
//
//    /**
//     * overloaded method setTaskOutput - gets only 3 parameters
//     *
//     * @param task
//     * @param dataSetXml
//     * @param personName
//     * @throws HumanTaskException
//     */
//    public void setTaskOutput(Long taskId, String dataSetXml, String personName) throws HumanTaskException {
//
//        this.setTaskOutput(task, dataSetXml, null, personName);
//    }

//    /**
//     * method that sets task output
//     *
//     * @param task
//     * @param dataSetXml
//     * @param pName
//     * @param personName
//     * @throws HumanTaskException
//     */
//    public void setTaskOutput(Long taskId, String dataSetXml, String pName, String personName) throws HumanTaskException {
//
//        Person person = assigneeDao.getPerson(personName);
//
//        if (!person.equals(task.getActualOwner())) {
//            log.error(person + " doesn't have the permission to set task output");
//            throw new HumanTaskException(person + " doesn't have the permission to set task output");
//        }
//
//        task.setOutput(pName, dataSetXml);
//        taskDao.update(task);
//    }

//    /**
//     * overloaded getting task output
//     *
//     * @param task
//     * @param personName
//     * @return
//     * @throws HumanTaskException
//     */
//    public String getOutput(Long taskId, String personName) throws HumanTaskException {
//
//        return this.getOutput(task, null, personName);
//    }

//    /**
//     * getting task output
//     *
//     * @param task
//     * @param partName
//     * @param personName
//     * @return
//     * @throws HumanTaskException
//     */
//    public String getOutput(Long taskId, String partName, String personName) throws HumanTaskException {
//
//        Person person = assigneeDao.getPerson(personName);
//
//        if (!person.equals(task.getActualOwner()) && !person.equals(task.getBusinessAdministrators())) {
//            log.error(person + " doesn't have the permission to get task output");
//            throw new HumanTaskException(person + " doesn't have the permission to get task output");
//        }
//
//        return task.getOutput(partName);
//    }

//    /**
//     * Deletes output.
//     *
//     * @param task
//     */
//    public void deleteOutput(Long taskId) {
//        task.deleteOutput();
//        taskDao.update(task);
//    }

//    /**
//     * complete task with fault response
//     *
//     * @param task
//     * @param personName
//     * @throws HumanTaskException
//     * @throws IllegalArgumentFault
//     */
//    public void failTask(Long taskId, String faultName, String faultData, String personName) throws HumanTaskException {
//
//        Person person = assigneeDao.getPerson(personName);
//
//        if (!person.equals(task.getActualOwner())) {
//            log.error(person + " cannot complete the task with fault response");
//            throw new HumanTaskException(person + " cannot complete the task with fault response");
//        }
//
//        if (faultName == null || faultData == null) {
//            log.error("none of faultName and faultXml cannot be null");
//            throw new IllegalArgumentException("none of faultName and faultXml cannot be null");
//        }
//
//        if (!task.findFault(faultName)) {
//            log.error("Illegal operation fault - there is no such fault name");
//            throw new RuntimeException("there is no such fault name");
//        }
//
//        // TODO ask fo
//        // this.setFault(faultName, faultData);
//
//        task.setStatus(Status.FAILED);
//        taskDao.update(task);
//
//    }

//    /**
//     * set fault name and fault Data
//     *
//     * @param task
//     * @param faultName
//     * @param faultData
//     * @param personName
//     * @throws HumanTaskException
//     * @throws IllegalOperationFault
//     */
//    public void setFault(Long taskId, String faultName, String faultData, String personName) throws HumanTaskException {
//
//        Person person = assigneeDao.getPerson(personName);
//
//        if (!person.equals(task.getActualOwner())) {
//            log.error(person + " cannot set the fault");
//            throw new HumanTaskException(person + " cannot set the fault");
//        }
//
//        if (!task.findFault(faultName)) {
//            log.error("Illegal operation fault - there is no such fault name");
//            throw new RuntimeException("there is no such fault name");
//        }
//
//        task.setFault(faultName, faultData);
//        taskDao.update(task);
//    }

//    /**
//     * delete fault name and fault data
//     *
//     * @param task
//     * @param personName
//     * @throws HumanTaskException
//     */
//    public void deleteFault(Long taskId, String personName) throws HumanTaskException {
//        Person person = assigneeDao.getPerson(personName);
//
//        if (!person.equals(task.getActualOwner())) {
//            log.error(person + " cannot remove foult form task " + task);
//            throw new HumanTaskException(person + " cannot remove foult form task " + task);
//        }
//
//        task.deleteFault();
//        taskDao.update(task);
//
//    }

//    public String getFault(Long taskId, String personName) throws HumanTaskException {
//
//        Person person = assigneeDao.getPerson(personName);
//        if (person.equals(task.getActualOwner()) && person.equals(task.getBusinessAdministrators())) {
//            log.error(person + " cannot get task''s foult");
//            throw new HumanTaskException(person + " cannot get task''s foult");
//        }
//
//        // TODO it should return fault name && fault data
//        String fault = task.getFault();
//
//        // alternatywnie
//
//        String faultName = task.getFaultName();
//        String faultData = task.getFaultData();
//
//        return faultData;
//
//    }
//
//    /**
//     * Suspends task for given period of time.
//     * 
//     * @param task Task to process
//     * @param timePeriod Time of suspension in milliseconds
//     * @throws HTException Thrown in case of problems while processing task 
//     */
//    public void suspendUntilPeriod(Long taskId, long timePeriod) throws HTException {
//
//        Calendar cal = Calendar.getInstance();
//        cal.setTimeInMillis(System.currentTimeMillis() + timePeriod);
//        
//        suspendUntil(taskId, cal.getTime());
//    }
//
//    /**
//     * Suspends task until given point of time.
//     *
//     * @param task Task to be processed
//     * @param pointOfTime Point of time until which task is to be suspended
//     * @throws HTException Thrown in case of problems while processing task 
//     */
//    // TODO can be suspeneded?
//    public void suspendUntil(Long taskId, Date pointOfTime) throws HTException {
//
//        final Task task = locateTask(taskId);
//        task.setSuspentionTime(pointOfTime);
//        task.setStatus(Status.SUSPENDED);
//        taskDao.update(task);
//    }
//
//    /**
//     * Adds {@link Attachment} to the task.
//     *
//     * @param task Task to process
//     * @param attName Attachment name
//     * @param accessType Access type of attachment
//     * @param contentType Content type of attachment
//     * @param attachment Content of attachment
//     * @param person Person processing task
//     * @throws HTException  Thrown if specified person doesn't have permission to process task or in case of problems while updating task
//     */
//    @Transactional(propagation = Propagation.REQUIRED)
//    public void addAttachment(Long taskId, String attName, String accessType, String contentType, String attachment, Person person) throws HTException {
//
//        final Task task = locateTask(taskId);
//        if (!(person.equals(task.getActualOwner()) || task.getBusinessAdministrators().contains(person))) {
//            log.error(person + "cannot add attachemnt");
//            throw new HTException(person + "cannot add attachemnt");
//        }
//
//        final Attachment att = new Attachment();
//        att.setName(attName);
//        att.setAccessType(accessType);
//        att.setContentType(contentType);
//
//        final Calendar cal = Calendar.getInstance();
//        att.setAttachedAt(cal.getTime());
//        att.setUserId(person.getId());
//        att.setAttachment(attachment);
//        task.addAttachment(att);
//
//        taskDao.update(task);
//    }

    // public List<Attachment> getAttachments(Long taskId, String attachmentName, Assignee assignee) {
    //
    // List<Attachment> list = new ArrayList<Attachment>();
    //
    // for (Attachment att : task.getAttachments()) {
    // if (att.getName().equals(attachmentName)) {
    // list.add(att);
    // }
    // }
    // return list;
    // }
    //
//    
//    /**
//     * Adds potential owners to task.
//     *
//     * @param task Task to add owners to
//     * @param list List of persons to add to potential task owners
//     */
//    public void addPotentialOwner(Long taskId, List<Person> list) throws HTIllegalArgumentException{
//        //TODO: not very efficient perhaps overload method to take a task
//        for (Person person : list) {
//            this.addPotentialOwner(taskId, person);
//        }
//    }
//
//    // TODO move to Task
//    /**
//     * Adds potential owner to task.
//     * 
//     * @param task Task to add owner to
//     * @param assignee Person to add as potential owner
//     */
//    private void addPotentialOwner(Long taskId, Assignee assignee) throws HTIllegalArgumentException{
//
//        final Task task = locateTask(taskId);
//        Set<Assignee> owners = task.getPotentialOwners();
//        if (!owners.contains(assignee)) {
//            owners.add(assignee);
//        }
//        task.setPotentialOwners(owners);
//
//        taskDao.update(task);
//    }

    // // TODO przenies do DAO i raczej powinno byc to zrobione w spring-sie
    // @Transactional(propagation = Propagation.REQUIRED)
    // public void refresh() {
    //
    // Calendar cal = Calendar.getInstance();
    // cal.setTimeInMillis(System.currentTimeMillis());
    //
    // List<Task> list = taskDao.getTasksToResume(Status.SUSPENDED, cal.getTime());
    //
    // for (Long taskId : list) {
    // if (cal.getTime().compareTo(task.getSuspentionTime()) > 0) {
    // task.resume();
    // log.info("refreshing tasks status");
    // taskDao.update(task);
    // }
    // }
    // }

    /**
     * Sets taskDao to given object.
     * @param taskDao TaskDao to set
     */
    public void setTaskDao(TaskDao taskDao) {
        Validate.notNull(taskDao);
        this.taskDao = taskDao;
    }

    /**
     * Sets assigneeDao to given object.
     * @param assigneeDao AssigneeDao to set
     */
    public void setAssigneeDao(AssigneeDao assigneeDao) {
        Validate.notNull(assigneeDao);
        this.assigneeDao = assigneeDao;
    }

    /**
    * @see org.apache.hise.api.HumanTaskServices#getTaskInfo(java.lang.Long) 
     */
    public Task getTaskInfo(Long taskId) throws HTIllegalArgumentException {
        if (this.taskDao.exists(taskId)) {
            return this.taskDao.fetch(taskId);
        }

        throw new HTIllegalArgumentException("Task not found.");
    }

    /**
     * Retrieves {@link Task} from DAO.
     * @param taskId
     * @return
     * @throws HTIllegalArgumentException
     */
    private Task locateTask(Long taskId) throws HTIllegalArgumentException {
        
        Validate.notNull(taskId);

        Task task = this.taskDao.fetch(taskId);
        task.setAssigneeDao(assigneeDao);
        task.setHumanInteractionsManager(humanInteractionsManager);
        task.init();

        if (null == task) {
            throw new HTIllegalArgumentException("Cannot find Task", Long.toString(taskId));
        }

        return task;
    }

}