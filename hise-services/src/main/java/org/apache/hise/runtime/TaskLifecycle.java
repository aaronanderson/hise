package org.apache.hise.runtime;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.dao.Task.Status;

public class TaskLifecycle implements TaskStateListener {
    private static Log __log = LogFactory.getLog(TaskLifecycle.class);

    public void stateChanged(Task task, Status oldStatus, Status newStatus) {
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

            default:
                break;

            }

            if (isOk) {

                __log.debug("Changing Task status : " + task + " status from: " + oldStatus + " to: " + newStatus);

                if (newStatus.equals(Status.SUSPENDED)) {
                    task.getTaskDto().setStatusBeforeSuspend(oldStatus);
                } else {
                    Validate.isTrue(task.getTaskDto().getStatusBeforeSuspend() == null);
                }

                // this.addOperationComment(Operations.STATUS, status);
                // Status oldStatus = this.status;
                // this.status = status;

            } else {
                String msg = "Changing Task status : " + task + " status from: " + oldStatus + " to: " + newStatus + " is not allowed.";
                __log.error(msg);
                throw new IllegalStateException(msg);
            }

        } else {
            __log.debug("Changing Task status: " + this + " status from: NULL to: " + newStatus);
            Validate.isTrue(newStatus == Status.CREATED);
        }
    }

}
