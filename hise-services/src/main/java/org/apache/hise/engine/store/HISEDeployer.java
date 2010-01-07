package org.apache.hise.engine.store;

import org.apache.hise.engine.HISEEngine;

public class HISEDeployer {
    public HISEEngine engine;
    public HISEDD deploymentInfo;
    
    public void init() {
        engine.deploy(deploymentInfo);
    }
}
