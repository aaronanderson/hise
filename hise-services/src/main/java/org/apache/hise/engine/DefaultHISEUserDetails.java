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

package org.apache.hise.engine;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.Validate;
import org.apache.hise.api.HISEUserDetails;
import org.apache.hise.dao.HISEDao;
import org.apache.hise.dao.OrgEntity;
import org.apache.hise.dao.TaskOrgEntity.OrgEntityType;

/**
 * Default implementation serves user details from HISE DAO
 */
public class DefaultHISEUserDetails implements HISEUserDetails {
    private HISEDao hiseDao;
    
    public void setHiseDao(HISEDao hiseDao) {
        this.hiseDao = hiseDao;
    }

    public String getUserPassword(String user1) {
        OrgEntity user = hiseDao.find(OrgEntity.class, user1);
        return user == null ? null : user.getUserPassword();
    }
    
    public Collection<String> getUserGroups(String user1) {
        OrgEntity user = hiseDao.find(OrgEntity.class, user1);
        Collection<String> r = new ArrayList<String>();
        if (user != null) {
	        for (OrgEntity g : user.getUserGroups()) {
	            Validate.isTrue(g.getType() == OrgEntityType.GROUP);
	            r.add(g.getName());
	        }
        }
        return r;
    }
}
