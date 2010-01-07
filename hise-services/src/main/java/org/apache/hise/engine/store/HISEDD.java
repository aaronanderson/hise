package org.apache.hise.engine.store;

import java.util.List;

import org.springframework.core.io.Resource;

public class HISEDD {
    private List<TaskDD> tasksDI;
    private Resource humanInteractionsResource;

    public List<TaskDD> getTasksDI() {
        return tasksDI;
    }

    public void setTasksDI(List<TaskDD> tasksDI) {
        this.tasksDI = tasksDI;
    }

    public Resource getHumanInteractionsResource() {
        return humanInteractionsResource;
    }

    public void setHumanInteractionsResource(Resource humanInteractionsResource) {
        this.humanInteractionsResource = humanInteractionsResource;
    }
}
