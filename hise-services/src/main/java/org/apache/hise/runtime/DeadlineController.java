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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.dao.Task.Status;
import org.apache.hise.lang.xsd.htd.TDeadline;
import org.apache.hise.lang.xsd.htd.TDeadlines;
import org.apache.hise.lang.xsd.htd.TExpression;

public class DeadlineController implements TaskStateListener {
    private static Log __log = LogFactory.getLog(DeadlineController.class);

    public void stateChanged(Task task, Status oldStatus, Status newStatus) {
        __log.debug(task + " " + oldStatus + " " + newStatus);
        
        TDeadlines deadlines = task.getTaskDefinition().gettTask().getDeadlines();
        if (deadlines == null) {
            deadlines = new TDeadlines();
        }
        
        if (newStatus.equals(Status.CREATED)) {
            //compute start deadlines
            computeDeadlines(task, deadlines.getStartDeadline(), false);
        } else if (newStatus.equals(Status.IN_PROGRESS)) {
            //delete start deadlines
            //compute completion deadlines
        } else if (newStatus.equals(Status.COMPLETED)) {
            //delete completion deadlines
        }
    }
    
    private void computeDeadlines(Task task, List<TDeadline> deadlines, boolean isCompletion) {
        for (TDeadline deadline : deadlines) {
            TExpression expr = deadline.getFor();
            Object v = task.getTaskEvaluator().evaluateExpression(expr);
            __log.debug("deadline " + v);
        }
    }
}
