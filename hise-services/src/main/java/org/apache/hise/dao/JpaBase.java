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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.MappedSuperclass;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@MappedSuperclass
public abstract class JpaBase {

    public abstract Object[] getKeys();
    
    @Override
    public int hashCode() {
        HashCodeBuilder b = new HashCodeBuilder();
        for (Object o : getKeys()) {
            b.append(o);
        }
        return b.toHashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        List<Object> l1 = new ArrayList<Object>();
        Collections.addAll(l1, getKeys());
        List<Object> l2 = new ArrayList<Object>();
        Collections.addAll(l2, ((JpaBase) obj).getKeys());
        return l1.equals(l2);
    }

    @Override
    public String toString() {
        ToStringBuilder b = new ToStringBuilder(this);
        for (Object o : getKeys()) {
            b.append(o);
        }
        return b.toString();
    }
}
