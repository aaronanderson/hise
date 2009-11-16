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

package org.apache.hise.lang.faults;

/**
 * Invoking an operation that does not apply to the task type (e.g., invoking claim on anotification) results in an illegalOperationFault.
 *
 * @author Warren Crossing
 */

public class HTIllegalOperationException extends HTException {
    public static final long serialVersionUID = 20090414103047L;
    
    /**
     * Information about illegal operation that was invoked.
     */
    private java.lang.String illegalOperation;

    /**
     * Creates empty HTIllegalOperationException.
     */
    public HTIllegalOperationException() {
        super();
    }
    
    /**
     * Creates HTIllegalOperationException and sets exception message.
     * @param message Exception message to set
     */
    public HTIllegalOperationException(String message) {
        super(message);
    }
    
    /**
     * Creates HTIllegalOperationException and sets exception message and cause.
     * @param message Exception message to set
     * @param cause Throwable that caused current exception
     */
    public HTIllegalOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates HTIllegalOperationException and sets exception message and information about illegal operation.
     * @param message Exception message to set
     * @param illegalOperation Additional information about illegal operation that was invoked
     */
    public HTIllegalOperationException(String message, java.lang.String illegalOperation) {
        super(message);
        this.illegalOperation = illegalOperation;
    }

    /**
     * Creates HTIllegalOperationException and sets exception message and information about illegal operation.
     * @param message Exception message to set
     * @param illegalOperation Additional information about illegal operation that was invoked
     * @param cause Throwable that caused current exception
     */
    public HTIllegalOperationException(String message, java.lang.String illegalOperation, Throwable cause) {
        super(message, cause);
        this.illegalOperation = illegalOperation;
    }

    /**
     * @return Information about illegal operation that was invoked
     */
    public java.lang.String getExceptionInfo() {
        return this.illegalOperation;
    }
}
