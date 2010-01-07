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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * Holds start or completion deadline information.
 * 
 * @author Witek Wołejszo
 * 
 */
@Entity
@Table(name = "DEADLINE")
public class Deadline extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "ddln_seq")
    @SequenceGenerator(name = "ddln_seq", sequenceName = "ddln_seq")
    private Long id;
    
    @Column(name = "EVENT_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date eventDate;

    @Column(name="ESCALATION_NAME", length = 256)
    private String escalationName;

    @Column(name="COMPLETION")
    private Boolean completion;
    
    @ManyToOne
    @JoinColumn(name = "TASK_ID")
    private Task task;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Deadline == false) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        Deadline rhs = (Deadline) obj;
        return new EqualsBuilder().append(this.id, rhs.id).isEquals();
    }

    @Override
    public int hashCode() {
        return (id == null ? 0 : id.hashCode());
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public String getEscalationName() {
        return escalationName;
    }

    public void setEscalationName(String escalationName) {
        this.escalationName = escalationName;
    }
    
    public Boolean getCompletion() {
        return completion;
    }

    public void setCompletion(Boolean completion) {
        this.completion = completion;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Long getId() {
        return id;
    }
}
