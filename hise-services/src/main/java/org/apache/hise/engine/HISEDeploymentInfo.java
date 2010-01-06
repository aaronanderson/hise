package org.apache.hise.engine;

import java.util.List;

import org.springframework.core.io.Resource;

public class HISEDeploymentInfo {
    public List<TaskDeploymentInfo> tasksDI;
    public Resource humanInteractionsResouce;

    public List<TaskDeploymentInfo> getTasksDI() {
        return tasksDI;
    }

    public void setTasksDI(List<TaskDeploymentInfo> tasksDI) {
        this.tasksDI = tasksDI;
    }

    public Resource getHumanInteractionsResouce() {
        return humanInteractionsResouce;
    }

    public void setHumanInteractionsResouce(Resource humanInteractionsResouce) {
        this.humanInteractionsResouce = humanInteractionsResouce;
    }
}
