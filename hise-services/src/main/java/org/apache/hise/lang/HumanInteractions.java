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

package org.apache.hise.lang;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Provides access methods to Human Interactions document defined
 * using http://www.example.org/WS-HT schema.
 * <p/>
 * Instance of this class can be obtained using {@link HumanInteractionsFactoryBean}.
 *
 * @author Witek Wołejszo
 */
public class HumanInteractions {

    private final Map<QName, TaskDefinition> taskDefinitions = new HashMap<QName, TaskDefinition>();

    public Map<QName, TaskDefinition> getTaskDefinitions() {
        return taskDefinitions;
    }
}
