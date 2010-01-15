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

package org.apache.hise.dao;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Task Assignee - member of generic human role.
 * 
 * @author Witek Wołejszo
 */
@Entity
public class TaskOrgEntity extends JpaBase {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Task task;

    private String name;

    public static enum OrgEntityType {
        USER, GROUP;
    }

    @Enumerated(value = EnumType.STRING)
    private OrgEntityType type;

    @Enumerated(value = EnumType.STRING)
    private GenericHumanRole genericHumanRole;

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OrgEntityType getType() {
        return type;
    }

    public void setType(OrgEntityType type) {
        this.type = type;
    }

    public GenericHumanRole getGenericHumanRole() {
        return genericHumanRole;
    }

    public void setGenericHumanRole(GenericHumanRole genericHumanRole) {
        this.genericHumanRole = genericHumanRole;
    }

    @Override
    public Object[] getKeys() {
        return new Object[] { id };
    }
}
