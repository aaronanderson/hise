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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Unresolved group of people.
 *
 * @author Witek Wołejszo
 * @author Kamil Eisenbart
 * @author Mateusz Lipczyński
 */
@Entity
@Table(name = "ASSIGNEE_GROUP")
public class Group extends Assignee {

    @Column(unique = true, nullable = false)
    private String name;

    /**
     * Unresolved group of people constructor.
     */
    public Group() {
        super();
    }

    /**
     * Unresolved group of people constructor.
     * @param name The group name.
     */
    public Group(String name) {
        super();
        Validate.notNull(name);
        this.setName(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    /**
     * Returns the group hash code.
     * @return group hash code
     */
    @Override
    public int hashCode() {
        
        if (this.id == null) {
            return new HashCodeBuilder(19, 21).append(this.name).toHashCode();
        }
        
        return new HashCodeBuilder(19, 21).append(this.id).toHashCode();
    }

    /**
     * Checks whether the group is equal to another object.
     * @param obj object to compare
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        
        if (this.id == null) {
            final String[] excludeFields = { "id" };
            return EqualsBuilder.reflectionEquals(this, obj, excludeFields);
        } 
        
        final String[] excludeFields = { "name" };
        return EqualsBuilder.reflectionEquals(this, obj, excludeFields);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", this.id).append("name", this.name).toString();
    }

}
