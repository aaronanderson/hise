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
 * An operation takes a well-defined set of parameters as its input. Passing an illegal
 * parameter or an illegal number of parameters results in the illegalArgumentFault
 * being thrown.
 *
 * @author Warren Crossing 
 */
public class HTIllegalArgumentException extends HTException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Information about illegal argument that was given.
     */
    private java.lang.String illegalArgument;

    /**
     * Creates empty HTIllegalArgumentException.
     */
    public HTIllegalArgumentException() {
        super();
    }
    
    /**
     * Creates HTIllegalArgumentException and sets exception message.
     * @param message Exception message to set
     */
    public HTIllegalArgumentException(String message) {
        super(message);
    }
    
    /**
     * Creates HTIllegalArgumentException and sets exception message and cause.
     * @param message Exception message to set
     * @param cause Throwable that caused current exception
     */
    public HTIllegalArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates HTIllegalArgumentException and sets exception message and information about illegal argument given.
     * @param message Exception message to set
     * @param illegalArgument Additional information about illegal argument given 
     */
    public HTIllegalArgumentException(String message, String illegalArgument) {
        super(message);
        this.illegalArgument = illegalArgument;
    }

    /**
     * Creates HTIllegalArgumentException and sets exception message and information about illegal argument given.
     * @param message Exception message to set
     * @param illegalArgument Additional information about illegal argument given 
     * @param cause Throwable that caused current exception
     */
    public HTIllegalArgumentException(String message, String illegalArgument, Throwable cause) {
        super(message, cause);
        this.illegalArgument = illegalArgument;
    }

    /**
     * @return Information about illegal argument that was given
     */
    public java.lang.String getExceptionInfo() {
        return this.illegalArgument;
    }
}
