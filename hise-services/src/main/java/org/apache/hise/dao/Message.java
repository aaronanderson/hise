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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Message part related to Task. It can be either part of input message or part of output message.
 * 
 * @author Witek Wołejszo
 */
@Entity
@Table(name = "MESSAGE")
public class Message extends JpaBase {
    
    private static final Log log = LogFactory.getLog(Message.class);
    
    @Id
    @GeneratedValue
    private Long id;
    
    private String partName;

    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    @Lob
    private String message;
    
    public Message() {}
    
    public Message(String partName, String message) {
        Validate.notNull(message);
        this.partName = partName;
        this.message = message;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public String getMessage() {
        return this.message;
    }

    public String getPartName() {
        return this.partName;
    }
    
    @Override
    public Object[] getKeys() {
        return new Object[] { id };
    }
}
