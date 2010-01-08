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

import java.math.BigDecimal;
import java.util.Date;

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
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.Index;

/**
 * Evaluated value of Task's presentation parameter.
 * @author Witek Wołejszo
 */
@Entity
@Table(name = "PRESENTATION_PARAMETERS")
public class PresentationParameter extends JpaBase {
    
    private static final Log log = LogFactory.getLog(PresentationParameter.class);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "prpr_seq")
    @SequenceGenerator(name = "prpr_seq", sequenceName = "prpr_seq")
    private Long id;
    
    private String name;

    private String stringValue;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateValue;
    
    private BigDecimal bigDecimalValue;
    
    private Double doubleValue;
    
    private Integer integerValue;
    
    private Boolean booleanValue;

    @Override
    public Object[] getKeys() {
        return new Object[] { id };
    }
    
//    @ManyToOne
//    @JoinColumn(name = "task_id", nullable = false)
//    @Index(name = "prpr_task_id_idx")
//    private Task task;
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getName() {
//        return this.name;
//    }
//    
//    /**
//     * @param stringValue the stringValue to set
//     */
//    public void setValue(Object value) {
//        
//        if (value instanceof String) {
//            
//            this.setValue((String)value);
//            
//        } else if (value instanceof BigDecimal) {
//
//            this.setValue((BigDecimal)value);
//
//        } else if (value instanceof Double) {
//
//            this.setValue((Double)value);
//
//        } else if (value instanceof Integer) {
//
//            this.setValue((Integer)value);
//
//        } else if (value instanceof Boolean) {
//            
//            this.setValue((Boolean)value);
//            
//        } else if (value instanceof Date) {
//            
//            this.setValue((Date)value);
//            
//        }        
//    }
//    
//    /**
//     * @param stringValue the stringValue to set
//     */
//    public void setValue(String stringValue) {
//        cleanValues();
//        this.stringValue = stringValue;
//    }
//
//    /**
//     * @param dateValue the dateValue to set
//     */
//    public void setValue(Date dateValue) {
//        cleanValues();
//        this.dateValue = dateValue;
//    }
//    
//    /**
//     * @param numericValue the numericValue to set
//     */
//    public void setValue(BigDecimal bigDecimalValue) {
//        cleanValues();
//        this.bigDecimalValue = bigDecimalValue;
//    }
//    
//    /**
//     * @param booleanValue the booleanValue to set
//     */
//    public void setValue(Boolean booleanValue) {
//        cleanValues();
//        this.booleanValue = booleanValue;
//    }
//    
//    /**
//     * @param doubleValue the doubleValue to set
//     */
//    public void setValue(Double doubleValue) {
//        cleanValues();
//        if (!doubleValue.isNaN()) {
//            this.doubleValue = doubleValue;
//        } else {
//            log.warn("NaN value passed. Storing null");
//        }
//    }
//    
//    /**
//     * @param integerValue the integerValue to set
//     */
//    public void setValue(Integer integerValue) {
//        cleanValues();
//        this.integerValue = integerValue;
//    }
//    
//    /**
//     * Returns value of proper type.
//     * @return value
//     */
//    public Object getValue() {
//
//        if (this.stringValue != null) {
//        
//            return this.stringValue;
//        
//        } else if (this.dateValue != null) {
//
//            return this.dateValue;
//        
//        } else if (this.bigDecimalValue != null) {
//            
//            return this.bigDecimalValue;
//            
//        } else if (this.doubleValue != null) {
//            
//            return this.doubleValue;
//            
//        } else if (this.integerValue != null) {
//            
//            return this.integerValue;
//            
//        } else if (this.booleanValue != null) {
//            
//            return this.booleanValue;
//            
//        } else {
//            
//            return null;
//        }
//    }
//    
//    /**
//     * @param task the task to set
//     */
//    public void setTask(Task task) {
//        this.task = task;
//    }
//
//
//    private void cleanValues() {
//        
//        this.stringValue = null;
//        this.dateValue = null;
//        this.bigDecimalValue = null;
//        this.doubleValue = null;
//        this.integerValue = null;
//        this.booleanValue = null;        
//    }
//    
//    /**
//     * Returns the presentation element hash code.
//     * @return presentation element hash code
//     */
//    @Override
//    public int hashCode() {
//        HashCodeBuilder hcb = new HashCodeBuilder();
//        return hcb.append(this.id).toHashCode();
//    }
//
//    /**
//     * Checks whether the presentation element is equal to another object.
//     * @param obj object to compare
//     * @return true if the objects are equal, false otherwise
//     */
//    @Override
//    public boolean equals(Object obj) {
//        if (obj instanceof PresentationParameter == false) {
//            return false;
//        }
//        if (this == obj) {
//            return true;
//        }
//        PresentationParameter pp = (PresentationParameter) obj;
//        return new EqualsBuilder().append(this.id, pp.id).isEquals();
//    }
    

}
