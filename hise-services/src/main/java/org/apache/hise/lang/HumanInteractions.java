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

import java.util.List;

import org.apache.hise.engine.PropertyBasedPeopleQuery;
import org.w3c.dom.Document;


/**
 * Provides access methods to Human Interactions document defined
 * using http://www.example.org/WS-HT schema.
 * <p/>
 * Instance of this class can be obtained using {@link HumanInteractionsFactoryBean}.
 *
 * @author Witek Wołejszo
 */
public class HumanInteractions {

    private Document document;

    private List<TaskDefinition> taskDefinitions;
    
    private PropertyBasedPeopleQuery peopleQuery;

    /**
     * Private constructor to prevent instantiation.
     */
    private HumanInteractions() {
    }

    /**
     * Constructor called by {@link HumanInteractionsManager implementation}.
     * @param document the human interactions DOM document
     * @param peopleQuery
     */
    public HumanInteractions(Document document, PropertyBasedPeopleQuery peopleQuery) {
        super();
        this.setDocument(document);
        this.peopleQuery = peopleQuery;
    }

    public void setTaskDefinitions(List<TaskDefinition> taskDefinitions) {
        this.taskDefinitions = taskDefinitions;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Document getDocument() {
        return document;
    }

    public List<TaskDefinition> getTaskDefinitions() {
        return taskDefinitions;
    }

}
