package org.apache.hise.engine.store;

import java.util.List;

import org.springframework.core.io.Resource;

public class HISEDD {
    private List<TaskDD> tasksDI;
    private Resource humanInteractionsResouce;

    public List<TaskDD> getTasksDI() {
        return tasksDI;
    }

    public void setTasksDI(List<TaskDD> tasksDI) {
        this.tasksDI = tasksDI;
    }

    public Resource getHumanInteractionsResouce() {
        return humanInteractionsResouce;
    }

    public void setHumanInteractionsResouce(Resource humanInteractionsResouce) {
        this.humanInteractionsResouce = humanInteractionsResouce;
    }
}
