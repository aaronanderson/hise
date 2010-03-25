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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.dao.GenericHumanRole;
import org.apache.hise.dao.Job;
import org.apache.hise.dao.Message;
import org.apache.hise.dao.TaskOrgEntity;
import org.apache.hise.dao.Task.Status;
import org.apache.hise.lang.xsd.htd.TDeadline;
import org.apache.hise.lang.xsd.htd.TDeadlines;
import org.apache.hise.lang.xsd.htd.TEscalation;
import org.apache.hise.runtime.TaskEvaluator.EscalationResult;
import org.apache.hise.utils.DOMUtils;
import org.w3c.dom.Node;

public class DeadlineController implements TaskStateListener {

    private static Log __log = LogFactory.getLog(DeadlineController.class);
    
    private Task task;

    
    public DeadlineController(Task task) {
        super();
        this.task = task;
    }

    public void stateChanged(Status oldStatus, Status newStatus) {
        __log.debug(task + " " + oldStatus + " " + newStatus);
        
        TDeadlines deadlines = task.getTaskDefinition().getDeadlines();
        if (deadlines == null) {
            deadlines = new TDeadlines();
        }
        
        if (newStatus == Status.CREATED) {
            computeDeadlines(deadlines.getStartDeadline(), false);
        } else if (newStatus == Status.IN_PROGRESS) {
            deleteDeadlines();
            computeDeadlines(deadlines.getCompletionDeadline(), true);
        } else if (newStatus == Status.COMPLETED || newStatus == Status.FAILED) {
            deleteDeadlines();
        }
    }
    
    private void deleteDeadlines() {
        __log.debug("clearing deadlines " + task.getTaskDto().getDeadlines());
        while (!task.getTaskDto().getDeadlines().isEmpty()) {
            Job j = task.getTaskDto().getDeadlines().iterator().next();
            task.getTaskDto().getDeadlines().remove(j);
            task.getHiseEngine().getHiseDao().remove(j);
        }
    }
    
    private void computeDeadlines(List<TDeadline> deadlines, boolean isCompletion) {
        for (TDeadline deadline : deadlines) {
            Date fire = task.getTaskEvaluator().evaluateDeadline(deadline);
            __log.debug("deadline fire date " + fire);
            
            for (TEscalation escalation : deadline.getEscalation()) {
                Job job = new Job();
                job.setAction("deadline");
                job.setTask(task.getTaskDto());
                job.setFire(fire);
                job.setDetails(TaskEvaluator.getEscalationKey(escalation, isCompletion));
                task.getHiseEngine().getHiseDao().persist(job);
                
                __log.debug("registering deadline " + job);
                task.getTaskDto().getDeadlines().add(job);
            }
        }
    }
    
    public void deadlineCrossed(Job deadline) throws HiseIllegalStateException {
        __log.debug("deadline crossed" + deadline);

        EscalationResult e = task.getTaskEvaluator().findEscalation(deadline.getDetails());
        if (e == null) {
            __log.warn("Can't find escalation " + deadline.getDetails() + " in task definition " + task);
        } else {
            Map<String, Node> msg = task.getTaskEvaluator().evaluateToParts(e.escalation.getToParts().size() == 0 ? null : e.escalation.getToParts().get(0));
            
            if (e.escalation.getReassignment() != null) {
                Set<TaskOrgEntity> result = task.getTaskEvaluator().evaluateGenericHumanRole(e.escalation.getReassignment().getPotentialOwners(), GenericHumanRole.POTENTIALOWNERS);
                task.forward(result);
            } else if (e.escalation.getLocalNotification() != null) {
                Node request = msg.get("request");
                task.getHiseEngine().receiveNotification(e.escalation.getLocalNotification().getReference(), request);
            } else {
                throw new NotImplementedException();
            }
        }
    }
}
