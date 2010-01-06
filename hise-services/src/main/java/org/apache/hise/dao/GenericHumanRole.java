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

/**
 * Enum type [spec 3.2]
 * @author Warren Crossing
 * @author Mateusz Lipczyński
 */
public enum GenericHumanRole {
    
    TASK_INITIATOR("taskInitiator"),
    TASK_STAKEHOLDERS("taskStakeholders"),
    POTENTIAL_OWNERS("potentialOwners"),
    ACTUAL_OWNER("actualOwner"),
    BUSINESS_ADMINISTRATORS("businessAdministrators"),
    NOTIFICATION_RECIPIENTS("notificationRecipients"),
    RECIPIENTS("recipients"),
    EXCLUDED_OWNERS("excludedOwners");

    GenericHumanRole(String value) {
        this.value = value;
    }

    private final String value;

    public static GenericHumanRole fromValue(String value){
        for (GenericHumanRole ghr : GenericHumanRole.values())
            if (null!=value && value.equals(ghr.toString()))
                return ghr;
        
        return null;
    }
    @Override
    public String toString() {
        return this.value;
    }

}