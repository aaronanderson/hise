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

package org.apache.hise.api;

import java.util.List;
import java.util.Map;

import org.apache.hise.runtime.Assignee;



/**
 * A People Query evaluation interface. People queries are evaluated during the creation of a human task or a notification. If a people query fails then the
 * human task or notification is created anyway.
 *
 * @author Kamil Eisenbart
 * @author Witek Wołejszo
 */
public interface PeopleQuery {

    /**
     * Evaluates assignees in logical people group.
     * @param logicalPeopleGroupName the logical people group name
     * @param parameters the map of parameters
     */
    List<Assignee> evaluate(String logicalPeopleGroupName, Map<String, Object> parameters);

}
