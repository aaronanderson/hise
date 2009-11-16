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

import java.io.Serializable;


/**
 * Basic DAO operations for domain objects extending {@link Base}.
 * @author Witek Wołejszo
 */
public interface BasicDao<T extends Base, ID extends Serializable> {

    /**
     * Retrieves domain object from persistent store.
     * @param id Identifier of the object requested
     * @return requested domain object
     */
    T fetch(ID id);

    /**
     * Saves domain object in persistent store.
     * @param entity Domain object to be updated
     */
    void update(T entity);

    /**
     * Creates domain object in persistent store.
     * @param entity Domain object to be created
     */
    void create(T entity);

}
