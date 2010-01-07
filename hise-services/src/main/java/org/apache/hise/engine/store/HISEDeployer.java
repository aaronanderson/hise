package org.apache.hise.engine.store;

import org.apache.hise.engine.HISEEngine;

public class HISEDeployer {
    public HISEEngine hiseEngine;
    public HISEDD deploymentInfo;
    
    public void init() {
        hiseEngine.deploy(deploymentInfo);
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
