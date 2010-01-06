package org.apache.hise.runtime;

import org.apache.hise.runtime.Task.Status;

public class ResponseHandler implements TaskStateListener {
    
    public void stateChanged(Task task, Status oldStatus, Status newStatus) {
        boolean result = false;
        if (newStatus.equals(Status.COMPLETED)) {
            result = true;
        } else if (newStatus.equals(Status.FAILED)) {
            result = false;
        } else return;
        //TODO:impl
//        task.get
        
    }

}
