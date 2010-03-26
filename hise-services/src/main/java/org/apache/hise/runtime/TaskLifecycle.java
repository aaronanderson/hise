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

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.dao.Task.Status;

public class TaskLifecycle implements TaskStateListener {
    
    private static Log __log = LogFactory.getLog(TaskLifecycle.class);

    private Task task;
    
    public TaskLifecycle(Task task) {
        super();
        this.task = task;
    }

    public void stateChanged(Status oldStatus, Status newStatus) throws HiseIllegalStateException {

        boolean isOk = false;

        // check if change is valid for current state
        if (oldStatus != null) {

            switch (oldStatus) {

            case CREATED:
                if (newStatus == Status.READY || newStatus == Status.RESERVED) {
                    isOk = true;
                }

                break;

            case READY:
                if (newStatus == Status.RESERVED || newStatus == Status.IN_PROGRESS || newStatus == Status.READY || newStatus == Status.SUSPENDED) {
                    isOk = true;
                }

                break;

            case RESERVED:
                if (newStatus == Status.IN_PROGRESS || newStatus == Status.READY || newStatus == Status.SUSPENDED || newStatus == Status.RESERVED) {
                    isOk = true;
                }

                break;

            case IN_PROGRESS:
                if (newStatus == Status.COMPLETED || newStatus == Status.FAILED || newStatus == Status.RESERVED || newStatus == Status.READY || newStatus == Status.SUSPENDED) {
                    isOk = true;
                }

                break;

            case SUSPENDED:
                if (newStatus == task.getTaskDto().getStatusBeforeSuspend()) {
                    isOk = true;
                }

                break;

            default:
                break;

            }

            if (isOk) {

                __log.debug("Changing Task status : " + task + " status from: " + oldStatus + " to: " + newStatus);

                if (newStatus == Status.SUSPENDED) {
                    task.getTaskDto().setStatusBeforeSuspend(oldStatus);
                } else if (oldStatus == Status.SUSPENDED) {
                    Validate.isTrue(task.getTaskDto().getStatusBeforeSuspend() == newStatus);
                    task.getTaskDto().setStatusBeforeSuspend(null);
                } else {
                    Validate.isTrue(task.getTaskDto().getStatusBeforeSuspend() == null);
                }

                // this.addOperationComment(Operations.STATUS, status);
                // Status oldStatus = this.status;
                // this.status = status;

            } else {
                String msg = "Changing Task status : " + task + " status from: " + oldStatus + " to: " + newStatus + " is not allowed.";
                __log.error(msg);
                throw new HiseIllegalStateException(msg);
            }

        } else {
            __log.debug("Changing Task status: " + this + " status from: NULL to: " + newStatus);
            Validate.isTrue(newStatus == Status.CREATED);
        }
    }

}
