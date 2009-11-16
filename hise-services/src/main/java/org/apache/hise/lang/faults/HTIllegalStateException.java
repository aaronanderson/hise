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

import org.apache.hise.runtime.Task;

/**
 * Invoking an operation that is not allowed in the current state of the
 * task results in an illegalStateFault.
 *
 * @author Warren Crossing 
 */
public class HTIllegalStateException extends HTException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Current task state.
     */
    private Task.Status illegalState;

    /**
     * Creates empty HTIllegalStateException.
     */
    public HTIllegalStateException() {
        super();
    }
    
    /**
     * Creates HTIllegalStateException and sets exception message.
     * @param message Exception message to set
     */
    public HTIllegalStateException(String message) {
        super(message);
    }
    
    /**
     * Creates HTIllegalStateException and sets exception message and cause.
     * @param message Exception message to set
     * @param cause Throwable that caused current exception
     */
    public HTIllegalStateException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates HTIllegalStateException and sets exception message and information about illegal task state.
     * @param message Exception message to set
     * @param illegalState Current state of the task
     */
    public HTIllegalStateException(String message, Task.Status illegalState) {
        super(message);
        this.illegalState = illegalState;
    }

    /**
     * Creates HTIllegalStateException and sets exception message and information about illegal task state.
     * @param message Exception message to set
     * @param illegalState Current state of the task
     * @param cause Throwable that caused current exception
     */
    public HTIllegalStateException(String message, Task.Status illegalState, Throwable cause) {
        super(message, cause);
        this.illegalState = illegalState;
    }

    /** 
     * @return Exception message, with name of illegal task state
     */
    public String getMessage() {
        return super.getMessage() + " " + illegalState;
    }
    
    /**
     * @return Current task state
     */
    public Task.Status getExceptionInfo() {
        return this.illegalState;
    }
}