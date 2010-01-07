package org.apache.hise.engine.store;

import org.apache.hise.engine.HISEEngine;

public class HISEDeployer {
    public HISEEngine engine;
    public HISEDeploymentInfo deploymentInfo;
    
    public void init() {
        engine.deploy(deploymentInfo);
    }
}
