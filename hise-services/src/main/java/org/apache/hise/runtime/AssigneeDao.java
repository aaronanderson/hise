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

package org.apache.hise.runtime;

import java.util.Set;

import org.springframework.stereotype.Repository;


/**
 * DAO operations specific to {@link Assignee}.
 * 
 * @author Witek Wołejszo
 */
@Repository
public interface AssigneeDao extends BasicDao<Assignee, Long> {

    /**
     * Returns {@link Person} by name.
     * 
     * @param name the name of a person.
     * @return the {@link Person} with specified name or null if no {@link Person} can be found
     */
    Person getPerson(String name);
    
    /**
     * Returns {@link Group} by name.
     * 
     * @param name the name of a group.
     * @return the {@link Group} with specified name or null if no {@link Group} can be found
     */
    Group getGroup(String name);
    
    /**
     * Persists assignees.
     * @param assignees The set of transient or not transient {@link Assignee}s.
     * @return The set of persisted assignees.
     */
    Set<Assignee> saveNotExistingAssignees(Set<Assignee> assignees); 

}
