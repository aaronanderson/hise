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

import java.util.HashSet;
import java.util.Set;

import org.apache.hise.lang.TaskDefinition;
import org.apache.hise.lang.faults.HTException;
import org.apache.hise.runtime.Assignee;
import org.apache.hise.runtime.AssigneeDao;
import org.apache.hise.runtime.GenericHumanRole;
import org.apache.hise.runtime.Person;
import org.apache.hise.runtime.Task;
import org.apache.hise.runtime.TaskDao;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;


public class TaskMockery extends Mockery {
    
    TaskDao taskDao;
    AssigneeDao assigneeDao;
    
    Task task = null;
    
    Person jacek = new Person("Jacek");
    Person witek = new Person("Witek");
    Person admin = new Person("admin");

    public TaskMockery(TaskDao taskDao, AssigneeDao assigneeDao) {
        this.assigneeDao = assigneeDao;
        this.taskDao = taskDao;
    }

    public Task getGoodTaskMock(boolean onlyOnePotentialOwner) {

        setImposteriser(ClassImposteriser.INSTANCE);  
       
        final TaskDefinition taskDefinition = mock(TaskDefinition.class);

        final Set<Assignee> assignees = new HashSet<Assignee>();
        assignees.add(jacek);
        
        final Set<Assignee> stakeholders = new HashSet<Assignee>();
        stakeholders.add(jacek);

        if (!onlyOnePotentialOwner) {
            assignees.add(witek);
            stakeholders.add(witek);
        }

        this.assigneeDao.create(jacek);
        this.assigneeDao.create(witek);
        this.assigneeDao.create(admin);

        checking(new Expectations() {{
            try{
                allowing(taskDefinition).getTaskName();
                will(returnValue("Task1"));
                allowing(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.POTENTIAL_OWNERS, with(any(Task.class)));
                will(returnValue(assignees));
                allowing(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.BUSINESS_ADMINISTRATORS, with(any(Task.class)));
                will(returnValue(new HashSet()));
                allowing(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.EXCLUDED_OWNERS, with(any(Task.class)));
                will(returnValue(new HashSet()));
                allowing(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.NOTIFICATION_RECIPIENTS, with(any(Task.class)));
                will(returnValue(new HashSet()));
                allowing(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.TASK_STAKEHOLDERS, with(any(Task.class)));
                will(returnValue(stakeholders));
            } catch (Exception e){
                
                e.printStackTrace();
                
            }
        }});

        try {
            
            task = new Task();
            task.init(taskDefinition, null, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ClaimApprovalRequest><cust><firstname>witek</firstname></cust><amount>1</amount></ClaimApprovalRequest>");

            if (onlyOnePotentialOwner) {
                Assert.assertTrue(task.getActualOwner().equals(jacek));
            } else {
                Assert.assertNull(task.getActualOwner());
            }
            
        } catch (HTException ex) {
           
            ex.printStackTrace();
        }

        taskDao.create(task);
      
        return task;
    }

    public Person getImpossibleOwner() {
       return admin;
    }
    
    public Person getPossibleOwner() {
        return jacek;
    }
    
}