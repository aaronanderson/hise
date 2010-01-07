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
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.runtime.Task;

/**
 * Task content.
 *
 * @author Witek Wołejszo
 * @author Mateusz Lipczyński
 */
@Entity
@Table(name = "TASK_COMMENT")
public class Comment extends Base {
    
    @Transient
    private final Log log = LogFactory.getLog(Comment.class);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "cmmt_seq")
    @SequenceGenerator(name = "cmmt_seq", sequenceName = "cmmt_seq")
    private Long id;

    @Column(name = "COMMENT_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Column(length = 4096)
    private String content;

    @ManyToOne
    @JoinColumn(name = "TASK_ID")
    private Task task;
    
    /***************************************************************
     * Constructor                                                 *
     ***************************************************************/

    /**
     * Creates {@link Comment}.
     */    
    public Comment() {
        super();
    }
    
    /**
     * Creates {@link Comment}.
     */
    public Comment(String content, Task task) {
        this.content = content;
        this.date = new Date();
        this.task = task;
    }
    
    /***************************************************************
     * Getters & setters                                           *
     ***************************************************************/

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return this.date;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

    public Task getTask() {
        return this.task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Comment == false) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        Comment rhs = (Comment) obj;
        return new EqualsBuilder().append(this.id, rhs.id).isEquals();
    }

    @Override
    public int hashCode() {
        return (id == null ? 0 : id.hashCode());
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", this.id).append("date", this.date).append("comment", this.content).toString();
    }
}
