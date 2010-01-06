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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.api.HumanInteractionsManager;
import org.apache.hise.dao.Assignee;
import org.apache.hise.dao.AssigneeDao;
import org.apache.hise.dao.GenericHumanRole;
import org.apache.hise.dao.Group;
import org.apache.hise.dao.Message;
import org.apache.hise.dao.Person;
import org.apache.hise.lang.TaskDefinition;
import org.apache.hise.lang.faults.HTException;
import org.apache.hise.runtime.Task;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.theories.suppliers.TestedOn;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;


/**
 * {@link Task} class unit tests.
 *
 * @author Witek Wołejszo
 * @author Mateusz Lipczyński
 */
@ContextConfiguration(locations = "classpath:/test.xml")
public class TaskUnitTest extends AbstractTransactionalJUnit4SpringContextTests {

    private final Log log = LogFactory.getLog(TaskUnitTest.class);
    
    private String xmlRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ClaimApprovalRequest><cust><firstname>witek</firstname></cust><amount value=\"1.3\">1.2</amount></ClaimApprovalRequest>";
    
    @Resource
    private HumanInteractionsManager humanInteractionsManager;
    
    @Resource
    private AssigneeDao assigneeDao;
    
    public HumanInteractionsManager getHumanInteractionsManager() {
        return humanInteractionsManager;
    }

    public void setHumanInteractionsManager(HumanInteractionsManager humanInteractionsManager) {
        this.humanInteractionsManager = humanInteractionsManager;
    }
    
    public AssigneeDao getAssigneeDao() {
        return assigneeDao;
    }

    public void setAssigneeDao(AssigneeDao assigneeDao) {
        this.assigneeDao = assigneeDao;
    }

    private Task newTask() {
        Task t = new Task();
        t.setHumanInteractionsManager(humanInteractionsManager);
        t.setAssigneeDao(assigneeDao);
        return t;
    }
    
    /**
     * Tests Task constructor.
     * Scenario: 1 potential owner. Expected status: RESERVED.
     */
    @Test
    public void testInstatiationOnePotentialOwner() throws HTException {

        Mockery mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};

        final TaskDefinition taskDefinition = mockery.mock(TaskDefinition.class);
        
        final Set<Assignee> assignees = new HashSet<Assignee>();
        assignees.add(new Person("mateusz"));
        
        mockery.checking(new Expectations() {{
            one(taskDefinition).getTaskName(); will(returnValue(new QName("http://www.insurance.example.com/claims/", "Task1")));
            atLeast(1).of(taskDefinition).evaluateHumanRoleAssignees(with(any(GenericHumanRole.class)), with(any(Task.class))); will(returnValue(assignees));
        }});

        Task task = newTask();
        task.init(taskDefinition, null, this.xmlRequest);
        assertEquals(Task.Status.RESERVED, task.getStatus());

        mockery.assertIsSatisfied();
    }

    /**
     * Tests Task constructor.
     * Scenario: no potential owners. Expected status: CREATED.
     * TODO ready!!! created in case of many ponetial owners!!!
     */
    @Test
    public void testInstatiationNoPotentialOwners() throws HTException {

        Mockery mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};

        final TaskDefinition taskDefinition = mockery.mock(TaskDefinition.class);
        final Set<Assignee> assignees = new HashSet<Assignee>();
        assignees.add(new Person("mateusz"));

        mockery.checking(new Expectations() {{
            one(taskDefinition).getTaskName(); will(returnValue(new QName("http://www.insurance.example.com/claims/", "Task1")));
            //potential owners
            one(taskDefinition).evaluateHumanRoleAssignees(with(any(GenericHumanRole.class)), with(any(Task.class))); will(returnValue(Collections.EMPTY_SET));
            //other roles
            atLeast(1).of(taskDefinition).evaluateHumanRoleAssignees(with(any(GenericHumanRole.class)), with(any(Task.class))); will(returnValue(assignees));
        }});

        Task task = newTask();
        task.init(taskDefinition, null, this.xmlRequest);
        assertEquals(Task.Status.CREATED, task.getStatus());

        mockery.assertIsSatisfied();
    }

    /**
     * Tests Task constructor.
     * Scenario: 2 potential owners. Expected status: READY.
     */
    @Test
    public void testInstatiationManyPotentialOwners() throws HTException {

        Mockery mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};

        final TaskDefinition taskDefinition = mockery.mock(TaskDefinition.class);
        
        final Set<Assignee> assignees = new HashSet<Assignee>();
        assignees.add(new Person("mateusz"));
        assignees.add(new Person("witek"));
        
        mockery.checking(new Expectations() {{
            one(taskDefinition).getTaskName(); will(returnValue(new QName("http://www.insurance.example.com/claims/", "Task1")));
            atLeast(1).of(taskDefinition).evaluateHumanRoleAssignees(with(any(GenericHumanRole.class)), with(any(Task.class))); will(returnValue(assignees));
        }});

        Task task = newTask();
        task.init(taskDefinition, null, this.xmlRequest);
        assertEquals(Task.Status.READY, task.getStatus());

        mockery.assertIsSatisfied();
    }

    /**
     * Tests Task.nominateActualOwner method.
     * 1. Input: Empty assignees. Output: null.
     * 2. Input: One person and a group assigned. Output: that person.
     * 3. Input: Two persons and a group. Output: null.
     */
    @Test
    public void testNominateActualOwner() {
        
        Set<Assignee> assignees = new HashSet<Assignee>();
        Task instance = new Task();
        Person result = instance.nominateActualOwner(assignees);
        assertEquals(null, result);

        Person person1 = new Person("mateusz");
        Group group = new Group ("pracownicy DON");
        assignees.add(group);
        assignees.add(person1);
        result = instance.nominateActualOwner(assignees);
        assertEquals(person1, result);

        Person person2 = new Person("ww");
        assignees.add(person2);
        result = instance.nominateActualOwner(assignees);
        assertEquals(null, result);
    }
    
    @Test
    public void testEvaluateXPathGetInput_StringElement() throws HTException {
        
        Task t = new Task();
        t.getInput().put("ClaimApprovalRequest", new Message(this.xmlRequest));

        Object o = t.evaluateXPath("htd:getInput('ClaimApprovalRequest')/cust/firstname", XPathConstants.STRING);

        assertNotNull(o);
        assertTrue(o instanceof String);
        assertEquals("witek", o.toString());
    }
    
    @Test
    public void testEvaluateXPathGetInput_DoubleElement() {
        
        Task t = new Task();
        t.getInput().put("ClaimApprovalRequest", new Message(this.xmlRequest));
        
        Object o = t.evaluateXPath("htd:getInput('ClaimApprovalRequest')/amount", XPathConstants.NUMBER);

        assertNotNull(o);
        assertTrue(o instanceof Double);
        assertEquals(Double.valueOf(1.2), o);
    }

    @Test
    public void testEvaluateXPathGetInput_DoubleAttr() {

        Task t = new Task();
        t.getInput().put("ClaimApprovalRequest", new Message(this.xmlRequest));

        Object o = t.evaluateXPath("htd:getInput(\"ClaimApprovalRequest\")/amount/@value", XPathConstants.NUMBER);
        
        assertNotNull(o);
        assertTrue(o instanceof Double);
        assertEquals(Double.valueOf(1.3), o);
    }

}
