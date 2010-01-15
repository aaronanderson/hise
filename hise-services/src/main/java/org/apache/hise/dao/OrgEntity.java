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

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Task Assignee - member of generic human role.
 * @author Witek Wołejszo
 */
@Entity
@Table(name = "ORG_ENTITY")
public class OrgEntity extends JpaBase {

//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO, generator = "asgn_seq")
//    @SequenceGenerator(name = "asgn_seq", sequenceName = "asgn_seq")
//    protected Long id;

    @Id
    private String name;

    @Enumerated(value = EnumType.STRING)
    private TaskOrgEntity.OrgEntityType type;

    private String userPassword;
    
    @OneToMany(cascade = {CascadeType.ALL})
    @JoinTable(name="USER_GROUPS")
    private Collection<OrgEntity> userGroups = new HashSet<OrgEntity>();

    
    public Collection<OrgEntity> getUserGroups() {
        return userGroups;
    }

    public void addToGroup(OrgEntity group) {
//        userGroups.add(group.getName());
        userGroups.add(group);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public TaskOrgEntity.OrgEntityType getType() {
        return type;
    }

    public void setType(TaskOrgEntity.OrgEntityType type) {
        this.type = type;
    }

    @Override
    public Object[] getKeys() {
        return new Object[] {name} ;
    }
}
