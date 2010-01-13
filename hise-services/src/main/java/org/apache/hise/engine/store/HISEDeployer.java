package org.apache.hise.engine.store;

import org.apache.commons.lang.Validate;
import org.apache.hise.engine.HISEEngine;
import org.apache.hise.engine.HISEEngine.TaskInfo;
import org.apache.hise.lang.HumanInteractions;
import org.apache.hise.lang.TaskDefinition;

public class HISEDeployer {
    public HISEEngine hiseEngine;
    public HISEDD deploymentInfo;
    
    public void init() throws CompileException {
        deploy(deploymentInfo);
    }
    
    public void deploy(HISEDD di) throws CompileException {
        HumanInteractions tasks = HumanInteractionsCompiler.compile(di.getHumanInteractionsResource());
        
        for (TaskDD t : di.getTasksDI()) {
            TaskDefinition d = tasks.getTaskDefinitions().get(t.getTaskName());
            Validate.notNull(d, "Can't find Task name specified in deployment descriptor " + t.getTaskName());
            TaskInfo ti = new HISEEngine.TaskInfo();
            ti.dd = t;
            ti.parent = di;
            ti.taskKey = HISEEngine.tasksKey(d.getInterface().getPortType(), d.getInterface().getOperation());
            ti.taskDefinition = d;
            hiseEngine.registerTask(ti);
        }
    }

    public void setHiseEngine(HISEEngine hiseEngine) {
        this.hiseEngine = hiseEngine;
    }

    public HISEDD getDeploymentInfo() {
        return deploymentInfo;
    }

    public void setDeploymentInfo(HISEDD deploymentInfo) {
        this.deploymentInfo = deploymentInfo;
    }
}
