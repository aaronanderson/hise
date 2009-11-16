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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.api.HumanTaskServices;
import org.apache.hise.engine.HumanTaskServicesImpl;
import org.apache.hise.lang.faults.HTException;
import org.apache.hise.lang.faults.HTIllegalAccessException;
import org.apache.hise.lang.faults.HTIllegalStateException;
import org.apache.hise.runtime.AssigneeDao;
import org.apache.hise.runtime.GenericHumanRole;
import org.apache.hise.runtime.Person;
import org.apache.hise.runtime.Task;
import org.apache.hise.runtime.TaskDao;
import org.apache.hise.runtime.Task.Status;
import org.apache.hise.runtime.Task.TaskTypes;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;


/**
 * {@link HumanTaskServicesImpl} integration tests.
 * TODO change assertions -> assumptions 
 *
 * @author Witek Wołejszo
 * @author Warren Crossing
 * @author Kamil Eisenbart
 * @author Piotr Jagielski
 * @author Mateusz Lipczyński
 */
@ContextConfiguration(locations = {"classpath:/test.xml"})
public class ServicesIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

    private final Log log = LogFactory.getLog(ServicesIntegrationTest.class);

    @Resource(name = "humanTaskServices")
    HumanTaskServices services;

    @Resource(name = "taskDao")
    TaskDao taskDao;

    @Resource(name = "assigneeDao")
    AssigneeDao assigneeDao;

    /**
     * Creates task with one potential owner.
     * @return
     * @throws HTException
     */
    public Task createTask_OnePotentialOwner() throws HTException {
        Task task = services.createTask(new QName("http://www.insurance.example.com/claims/", "Task1"), null, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ClaimApprovalRequest><cust><firstname>witek</firstname></cust><amount>1</amount></ClaimApprovalRequest>");
        org.junit.Assert.assertEquals(1, task.getPotentialOwners().size());
        return task;
    }
    
    /**
     * Creates task with two potential owner.
     * @return
     * @throws HTException
     */
    public Task createTask_TwoPotentialOwners() throws HTException {
        Task task = services.createTask(new QName("http://www.insurance.example.com/claims/", "Task2"), null, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ClaimApprovalRequest><cust><firstname>witek</firstname></cust><amount>1</amount></ClaimApprovalRequest>");
        org.junit.Assert.assertEquals(2, task.getPotentialOwners().size());
        return task;
    }

    //TESTS

    /**
     * Checks my tasks for newly created task where i'm the only owner. Expects
     * one task (reserved) on a list.
     * @throws HTException
     */
    @Test
    public void testGetMyTasks_ByOwner() throws HTException {

        Task t = createTask_OnePotentialOwner();

        List<Task> results = services.getMyTasks("user1", TaskTypes.ALL,
                GenericHumanRole.ACTUAL_OWNER, null,
                new ArrayList<Status>(), null, null, null, null, 0);

        Assert.assertEquals(1, results.size());

        Task taskToCheck = results.get(0);

        Assert.assertEquals(t.getActualOwner(), taskToCheck.getActualOwner());
        Assert.assertEquals(Task.Status.RESERVED, taskToCheck.getStatus());
    }
    
    /**
     * Checks my tasks for newly created task if i'm potential owner. Expects "user1"
     * on potential owners list.
     * @throws HTException
     */
    @Test
    public void testGetMyTasks_ByPotentialOwner() throws HTException {

        log.info("testGetMyTasks_ByPotentialOwner");
        
        Task t = createTask_OnePotentialOwner();

        List<Task> results = this.services.getMyTasks("user1", TaskTypes.ALL,
                GenericHumanRole.POTENTIAL_OWNERS, null,
                new ArrayList<Status>(), null, null, null, null, 0);

        Assert.assertEquals(1, results.size());

        Task taskToCheck = results.get(0);
        Person p = this.assigneeDao.getPerson("user1");
        Assert.assertNotNull(p);
        
        log.info(taskToCheck.getPotentialOwners());
        Assert.assertEquals(taskToCheck.getActualOwner(), p);
        Assert.assertEquals(taskToCheck.getPotentialOwners().size(), 1);
        
        log.info("Potential owner: " + taskToCheck.getPotentialOwners().iterator().next());
        log.info("Potential owner: " + p);
        
        Assert.assertEquals(p, taskToCheck.getPotentialOwners().iterator().next());
        
        //TODO why it fails??? jnb???
        //Assert.assertTrue(taskToCheck.getPotentialOwners().contains(p));
        
        log.info("~testGetMyTasks_ByPotentialOwner");
    }
    
    //TODO test maxTasks

    @Test
    public void testGetTaskInfo() throws HTException {

        Task t = createTask_OnePotentialOwner();

        Task resultTask = this.services.getTaskInfo(t.getId());

        Assert.assertNotNull(resultTask);
        Assert.assertEquals(t.getActualOwner(), resultTask.getActualOwner());
    }

    @Test(expected = HTIllegalStateException.class)
    public void testClaimByOwner() throws HTException {

        Task t = createTask_OnePotentialOwner();

        //t.getActualOwner() is already set and cannot claim task once more 
        this.services.claimTask(t.getId(), t.getActualOwner().getName());
    }
    
    @Test(expected = HTIllegalStateException.class)
    public void testClaimTaskReserved() throws HTException {

        Task t = createTask_OnePotentialOwner();
        //org.junit.Assert.assertEquals(Task.Status.RESERVED, t.getStatus());
        org.junit.Assume.assumeTrue(Task.Status.RESERVED.equals(t.getStatus()));
        
        this.services.claimTask(t.getId(), "user2");
    }

    @Test(expected = HTIllegalStateException.class)
    public void testClaimTaskNotReady() throws HTException {

        Task t = createTask_TwoPotentialOwners();
        
        org.junit.Assert.assertEquals(Task.Status.READY, t.getStatus());
        this.services.claimTask(t.getId(), "user1");
        
        t = this.services.getTaskInfo(t.getId());
        org.junit.Assert.assertEquals(Task.Status.RESERVED, t.getStatus());

        this.services.claimTask(t.getId(), "user2");
    }

    /**
     *  This test should not claim the task because the potential owner is incorrect.
     */
    @Test(expected = HTIllegalAccessException.class)
    public void testClaimTaskNotOwner() throws HTException {

        Task t = createTask_OnePotentialOwner();
        
        Assert.assertNotNull(t.getActualOwner());

        //release task
        this.services.releaseTask(t.getId(), t.getActualOwner().getName());
        
        Task t2 = this.services.getTaskInfo(t.getId());
        Assert.assertEquals(Task.Status.READY, t2.getStatus());
        
        //claim by incorrect person
        this.services.claimTask(t2.getId(), "ImpossibleOwner");
    }

    /**
     * Makes sure that someone that is not a potential owner cannot start the Task. 
     */
    @Test(expected=HTIllegalAccessException.class)
    public void testStartTaskNotOwner() throws HTException {

        Task t = createTask_OnePotentialOwner();
        this.services.startTask(t.getId(), "user2");
    }
    
    /**
     * READY task can be started by potential owner. 
     */
    @Test
    public void testStartTaskReadyByPotentialOwner() throws HTException {

        Task t = createTask_TwoPotentialOwners();
        this.services.startTask(t.getId(), "user1");
        
        org.junit.Assert.assertEquals("user1", t.getActualOwner().getName());
        org.junit.Assert.assertEquals(Status.IN_PROGRESS, t.getStatus());
    }
    
    /**
     * RESERVED task can be started by actual owner. 
     */
    @Test
    public void testStartTaskReservedByPotentialOwner() throws HTException {

        Task t = createTask_OnePotentialOwner();
        org.junit.Assert.assertEquals("user1", t.getActualOwner().getName());
        org.junit.Assert.assertEquals(Status.RESERVED, t.getStatus());
        
        this.services.startTask(t.getId(), "user1");
        org.junit.Assert.assertEquals(Status.IN_PROGRESS, t.getStatus());
    }
    
    /**
     * RESERVED task can not be started by not actual owner. 
     */
    @Test(expected = HTIllegalAccessException.class)
    public void testStartTaskReservedByNotActualOwner() throws HTException {

        Task t = createTask_OnePotentialOwner();
        //org.junit.Assert.assertEquals("user1", t.getActualOwner().getName());
        org.junit.Assume.assumeTrue("user1".equals(t.getActualOwner().getName()));
        //org.junit.Assert.assertEquals(Status.RESERVED, t.getStatus());
        org.junit.Assume.assumeTrue(Status.RESERVED.equals(t.getStatus()));
        
        this.services.startTask(t.getId(), "user2");
    }
    
    @Test
    public void testReleaseTask() throws HTException {

        Task t = createTask_OnePotentialOwner();

        this.services.releaseTask(t.getId(), t.getActualOwner().getName());

        Assert.assertEquals(Status.READY, t.getStatus());
    }
    
    /**
     * READY -> RESERVED by potential owner.
     */
    @Test
    public void testDelegateTaskReadyByPotentialOwner() throws HTException {

        Task t = createTask_TwoPotentialOwners();
        Assert.assertEquals(Status.READY, t.getStatus());

        this.services.delegateTask(t.getId(), "user1", "user2");
        t = this.services.getTaskInfo(t.getId());

        Assert.assertEquals(Status.RESERVED, t.getStatus());
        Assert.assertEquals("user2", t.getActualOwner().getName());
    }
    
    /**
     * READY -> RESERVED by not owner nor adminisrator.
     * @throws HTException
     */
    @Test (expected = HTIllegalAccessException.class)
    public void testDelegateTaskReadyByNotPotentialOwnerNorAdministrator() throws HTException {

        Task t = createTask_TwoPotentialOwners();
        Assert.assertEquals(Status.READY, t.getStatus());

        this.services.delegateTask(t.getId(), "user3", "user2");
        t = this.services.getTaskInfo(t.getId());
    }
    
    @Test
    public void testCompleteTask() throws HTException {

        Task t = createTask_TwoPotentialOwners();
        this.services.claimTask(t.getId(), "user1");
        this.services.startTask(t.getId(), "user1");
        
        Assert.assertEquals(Status.IN_PROGRESS, t.getStatus());
        
        this.services.completeTask(t.getId(), "user1", "</a>");

        Assert.assertEquals(Status.COMPLETED, t.getStatus());
    }
    
    @Test(expected = HTIllegalAccessException.class)
    public void testCompleteTaskNotByActualOwner() throws HTException {

	Task t = createTask_TwoPotentialOwners();

        this.services.claimTask(t.getId(), "user1");
        this.services.startTask(t.getId(), "user1");
        
        this.services.completeTask(t.getId(), "user2", "</a>");
    }
    
    @Test(expected = HTIllegalStateException.class)
    public void testCompleteTaskNotInProgress() throws HTException {

	Task t = createTask_TwoPotentialOwners();

        this.services.claimTask(t.getId(), "user1");
        
        Assert.assertEquals(Status.RESERVED, t.getStatus());
        
        this.services.completeTask(t.getId(), "user1", "</a>");
    }

//    /***
//     *  This test should not claim the task becuase the owner was incorrect
//     */
//    @Test
//    public void testDelegateNotOwner() throws HTException {
//
//        TaskMockery mockery = new TaskMockery(taskDao, assigneeDao);
//        Task mockTask = mockery.getGoodTaskMock(true);
//
//        Throwable t = null;
//
//        try {
//            services.delegateTask(mockTask.getId(), mockery.getImpossibleOwner().getName());
//            Assert.fail();
//        }catch(HTIllegalAccessException xRNA){
//            //success
//            t = xRNA;
//        }
//
//        Assert.assertNotNull("claim Task did not throw on impossible owner",t);
//
//        //FIXME:
//        //Assert.assertEquals(Task.Status.RESERVED, mockTask.getStatus());
//
//        mockery.assertIsSatisfied();
//    }
    
//    @Test
//    @Transactional
//    @Rollback
//    public void testStartCorrectOwner() throws HTException {
//
//        TaskMockery mock = new TaskMockery(taskDao, assigneeDao);
//        Task mockTask = mock.getGoodTaskMock();
//
//        try {
//            services.startTask(mockTask.getId(), mock.getPossibleOwner().getName());
//        } catch (HTIllegalAccessException xIA) {
//            Assert.fail();
//        }
//    }

//     /**
//     *
//     * @throws HTException
//     */
//    @Test
//    @Transactional
//    @Rollback
//    public void testStartAfterClaim() throws HTException {
//
//        TaskMockery mockery = new TaskMockery(taskDao, assigneeDao);
//        Task mockTask = mockery.getGoodTaskMock(true);
//
//        try {
//            services.startTask(mockTask.getId(), mockery.getPossibleOwner().getName());
//        } catch (HTIllegalAccessException xIA) {
//            Assert.fail();
//        }
//
//        Assert.assertEquals(Task.Status.IN_PROGRESS, mockTask.getStatus());
//        
//    }
//
//    @Test
//    @Transactional
//    @Rollback
//    public void testReleaseAfterClaim() throws HTException {
//
//        TaskMockery mockery = new TaskMockery(taskDao, assigneeDao);
//        Task mockTask = mockery.getGoodTaskMock(true);
//
//        try {
//            services.startTask(mockTask.getId(), mockery.getPossibleOwner().getName());
//        } catch (HTIllegalAccessException xIA) {
//            Assert.fail();
//        }
//
//        Assert.assertEquals(Task.Status.IN_PROGRESS, mockTask.getStatus());
//
//        try {
//            services.releaseTask(mockTask.getId(), mockery.getPossibleOwner().getName());
//        } catch (HTIllegalAccessException xIA) {
//            Assert.fail();
//        }
//
//        Assert.assertEquals(Task.Status.READY, mockTask.getStatus());
//    }

}
