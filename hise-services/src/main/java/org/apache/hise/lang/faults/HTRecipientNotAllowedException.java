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
public class HTRecipientNotAllowedException extends HTException {
    
    private static final long serialVersionUID = 1L;
    
    /** 
     * Name of person that was not authorized for executed operation (which caused the exception).
     */
    private String recipientNotAllowed;

    /**
     * Creates empty HTRecipientNotAllowedException.
     */
    public HTRecipientNotAllowedException() {
        super();
    }
    
    /**
     * Creates HTRecipientNotAllowedException and sets exception message.
     * @param message Exception message to set
     */
    public HTRecipientNotAllowedException(String message) {
        super(message);
    }
    
    /**
     * Creates HTRecipientNotAllowedException and sets exception message and cause.
     * @param message Exception message to set
     * @param cause Throwable that caused current exception
     */
    public HTRecipientNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates HTRecipientNotAllowedException and sets exception message and information about person rejected.
     * @param message Exception message to set
     * @param recipientNotAllowed Name of person that was not authorized to perform operation
     */
    public HTRecipientNotAllowedException(String message, String recipientNotAllowed) {
        super(message);
        this.recipientNotAllowed = recipientNotAllowed;
    }

    /**
     * Creates HTRecipientNotAllowedException and sets exception message, information about person rejected and exception cause.
     * @param message Exception message to set
     * @param recipientNotAllowed Name of person that was not authorized to perform operation
     * @param cause Throwable that caused current exception
     */
    public HTRecipientNotAllowedException(String message, String recipientNotAllowed, Throwable cause) {
        super(message, cause);
        this.recipientNotAllowed = recipientNotAllowed;
    }

    /** 
     * @return Exception message, with name of not allowed recipient added
     */
    public String getMessage() {
        return super.getMessage() + " " + recipientNotAllowed;
    }
    
    /**
     * @return Name of person that was not authorized for executed operation (which caused the exception)
     */
    public java.lang.String getExceptionInfo() {
        return this.recipientNotAllowed;
    }
}
