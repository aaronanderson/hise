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

import java.util.List;

import org.apache.hise.api.HumanTaskServices;
import org.apache.hise.runtime.Task.TaskTypes;
import org.springframework.stereotype.Repository;



/**
 * DAO operations specific to {@link Task}.
 * 
 * @author Witek Wołejszo
 * @author Warren Crossing
 */
@Repository
public interface TaskDao extends BasicDao<Task, Long> {

//    /**
//     * Returns all {@link Task}s currenty owned by specifed {@link Person}.
//     *
//     * @param   owner the owner's name
//     * @return  list of {@link Task}s
//     */
//    List<Task> getTasks(Person owner);

    /**
     * Returns tasks. See {@link HumanTaskServices#getMyTasks(String, TaskTypes, GenericHumanRole, String, List, String, String, Integer)}
     * for method contract.
     *
     * @param owner
     * @param taskType
     * @param genericHumanRole
     * @param workQueue
     * @param status
     * @param whereClause
     * @param orderByClause
     * @param createdOnClause
     * @param maxTasks
     * @param offset
     * @return
     */
    List<Task> getTasks(Assignee owner, TaskTypes taskType, GenericHumanRole genericHumanRole, String workQueue, List<Task.Status> status, String whereClause,
            String orderByClause, String createdOnClause, Integer maxTasks, Integer offset);

    /**
     * Checks if given entity exists.
     * @param primaryKey Primary key of the entity
     * @return true if entity exists false otherwise
     */
    boolean exists(Long primaryKey);
}
