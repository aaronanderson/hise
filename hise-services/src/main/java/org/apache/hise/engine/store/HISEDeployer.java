/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.hise.engine.store;

import org.apache.commons.lang.Validate;
import org.apache.hise.api.HISEEngine;
import org.apache.hise.engine.HISEEngineImpl;
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
            org.apache.hise.api.HISEEngine.TaskInfo ti = new org.apache.hise.api.HISEEngine.TaskInfo();
            ti.dd = t;
            ti.parent = di;
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
