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
 * By default, the identity of the person on behalf of which the operation is invoked is
 * passed to the task. When the person is not authorized to perform the operation the
 * illegalAccessFault and recipientNotAllowed is thrown in the case of tasks and
 * notifications respectively.
 * 
 * @author Warren Crossing 
 */
public class HTIllegalAccessException extends HTException {
    public static final long serialVersionUID = 20090414103047L;
    
    /**
     *  Additional information about illegal access.
     */
    private java.lang.String illegalAccess;

    /**
     * Creates empty HTIllegalAccessException.
     */
    public HTIllegalAccessException() {
        super();
    }
    
    /**
     * Creates HTIllegalAccessException and sets exception message.
     * @param message Exception message to set
     */
    public HTIllegalAccessException(String message) {
        super(message);
    }
    
    /**
     * Creates HTIllegalAccessException and sets exception message and cause.
     * @param message Exception message to set
     * @param cause Throwable that caused current exception
     */
    public HTIllegalAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates HTIllegalAccessException and sets exception message and information about illegal access.
     * @param message Exception message to set
     * @param illegalAccess Additional information about illegal access 
     */
    public HTIllegalAccessException(String message, java.lang.String illegalAccess) {
        super(message);
        this.illegalAccess = illegalAccess;
    }

    /**
     * Creates HTIllegalAccessException and sets exception message and information about illegal access.
     * @param message Exception message to set
     * @param illegalAccess Additional information about illegal access 
     * @param cause Throwable that caused current exception
     */
    public HTIllegalAccessException(String message, String illegalAccess, Throwable cause) {
        super(message, cause);
        this.illegalAccess = illegalAccess;
    }

    public String getMessage(){
        return super.getMessage() + " " + getExceptionInfo();
    }
    /**
     * @return Additional information about illegal access
     */
    public java.lang.String getExceptionInfo() {
        return this.illegalAccess;
    }
}
