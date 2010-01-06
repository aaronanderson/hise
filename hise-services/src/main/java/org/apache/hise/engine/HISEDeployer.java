package org.apache.hise.engine;

public class HISEDeployer {
    public HISEEngine engine;
    public HISEDeploymentInfo deploymentInfo;
    
    public void init() {
        engine.deploy(deploymentInfo);
    }
}
