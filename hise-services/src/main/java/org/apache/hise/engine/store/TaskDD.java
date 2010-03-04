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

import javax.xml.namespace.QName;

public class TaskDD {

    public QName taskName;
    public org.apache.hise.engine.jaxws.HISEJaxWSService handler;
    public org.apache.hise.engine.jaxws.HISEJaxWSClient sender;

    public QName getTaskName() {
        return taskName;
    }

    public void setTaskName(QName taskName) {
        this.taskName = taskName;
    }

    public Object getHandler() {
        return handler;
    }

    public void setHandler(org.apache.hise.engine.jaxws.HISEJaxWSService handler) {
        this.handler = handler;
    }

    public Object getSender() {
        return sender;
    }

    public void setSender(org.apache.hise.engine.jaxws.HISEJaxWSClient sender) {
        this.sender = sender;
    }

}
