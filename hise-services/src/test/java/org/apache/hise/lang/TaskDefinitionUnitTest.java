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

package org.apache.hise.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.api.HumanInteractionsManager;
import org.apache.hise.dao.Assignee;
import org.apache.hise.dao.GenericHumanRole;
import org.apache.hise.dao.Group;
import org.apache.hise.dao.Person;
import org.apache.hise.lang.TaskDefinition;
import org.apache.hise.lang.faults.HTException;
import org.apache.hise.runtime.Task;
import org.apache.hise.utils.TestUtil;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


/**
 * Test of {@link TaskDefinition} class.
 *
 * @author Witek Wołejszo
 */
@Ignore
public class TaskDefinitionUnitTest {

    private final Log log = LogFactory.getLog(TaskDefinitionUnitTest.class);

    private HumanInteractionsManager humanInteractionsManager;
    
    private static final String REQUEST = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ClaimApprovalRequest><priority>7</priority><cust><firstname>jan</firstname><lastname>kowalski</lastname></cust><amount>1</amount></ClaimApprovalRequest>";

    private TaskDefinition getTask1() {
        return humanInteractionsManager.getTaskDefinition(new QName("http://www.insurance.example.com/claims/", "Task1"));
    }
    
    @Before
    public void setUpTestContext() throws HTException {

        this.humanInteractionsManager = TestUtil.createHumanInteractionsManager("testHtd1-human-interaction.xml");
    }
    
    private Task newTask() {
        return new Task();
    }

    @Test
    public void testGetDescriptionPlain() throws HTException {       
        TaskDefinition td = getTask1();
        Task task = newTask();
        task.init(td, null, REQUEST);
        String description = td.getDescription("en-US", "text/plain", task);
        assertEquals("Approve this claim following corporate guideline #4711.0815/7 ...".trim(), description.trim());
    }
    
    @Test
    public void testGetPriority() throws HTException {       
        TaskDefinition td = getTask1();
        Task task = newTask();
        task.init(td, null, REQUEST);
        Integer priority = td.getPriority(task);
        assertEquals(Integer.valueOf(7), priority);
    }

    /**
     * Test people query implementation returns zero.
     * @throws HTException
     */
    @Test
    public void testEvaluateHumanRoleAssignees() throws HTException {
        
        Mockery mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};
        final Task task = mockery.mock(Task.class);
        
        TaskDefinition td = getTask1();
        
        Set<Assignee> assigneeList = td.evaluateHumanRoleAssignees(GenericHumanRole.POTENTIAL_OWNERS, task);
        Assert.assertEquals(1, assigneeList.size());
        
        Set<Assignee> bussinessAdministrators = td.evaluateHumanRoleAssignees(GenericHumanRole.BUSINESS_ADMINISTRATORS, task);
        Assert.assertEquals(4, bussinessAdministrators.size());
    }
    
    @Test
    public void testEvaluateHumanRoleAssigneesUnresolvedGroupOfPeople() throws HTException {
        
        Mockery mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};
        final Task task = mockery.mock(Task.class);
        
        TaskDefinition td = getTask1();
        Set<Assignee> assigneeList = td.evaluateHumanRoleAssignees(GenericHumanRole.BUSINESS_ADMINISTRATORS, task);
        
        Assert.assertTrue(assigneeList.contains(new Group("group1")));
        Assert.assertTrue(assigneeList.contains(new Group("group2")));
    }
    
    @Test
    public void testEvaluateHumanRoleAssigneesPeople() throws HTException {
        
        Mockery mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};
        final Task task = mockery.mock(Task.class);
        
        TaskDefinition td = getTask1();
        Set<Assignee> assigneeList = td.evaluateHumanRoleAssignees(GenericHumanRole.BUSINESS_ADMINISTRATORS, task);
        
        Assert.assertTrue(assigneeList.contains(new Person("user1")));
        Assert.assertTrue(assigneeList.contains(new Person("user2")));
    }

    @Test
    public void testGenericHumanRoleNotFoundInTaskDefinition() throws HTException {
        
        Mockery mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};
        final Task task = mockery.mock(Task.class);
        
        TaskDefinition td = getTask1();
        Set<Assignee> assigneeList = td.evaluateHumanRoleAssignees(GenericHumanRole.NOTIFICATION_RECIPIENTS, task);
        Assert.assertEquals(0, assigneeList.size());
    }

    @Test
    public void testGetSubject() throws HTException {
        TaskDefinition td = getTask1();
        Task task = newTask();
        task.init(td, null, REQUEST);
        String expResult = "Approve the insurance claim for PLN 1.0";
        String result = td.getSubject("en-US", task);
        log.debug("result: " + result);
        assertTrue(result.contains(expResult));
    }

    @Test
    public void testGetKey() throws HTException {
        TaskDefinition td = getTask1();
        QName result = td.getTaskName();
        assertEquals(new QName("http://www.insurance.example.com/claims/", "Task1"), result);
    }

    /**
     * Checks for existance and value of presenation parameter:
     * <htd:presentationParameter name="firstname" type="xsd:string">
     *     htd:getInput("ClaimApprovalRequest")/cust/firstname
     * </htd:presentationParameter>
     * 
     * @throws HTException
     */
    @Test
    public void testGetTaskPresentationParameters() throws HTException {
        
        TaskDefinition td = getTask1();

        Task task = newTask();
        task.init(td, null, REQUEST);
        
        Map<String, Object> result = td.getTaskPresentationParameters(task);
        
        assertTrue(result.containsKey("firstname"));
        assertTrue(result.containsKey("lastname"));
        assertTrue(result.containsKey("euroAmount"));

        log.info(result.get("firstname"));
        log.info(result.get("lastname"));
        log.info(result.get("euroAmount"));
        
        assertEquals("jan", result.get("firstname"));
    }

}
