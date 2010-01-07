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

package org.apache.hise.engine.jaxws;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Adds transactions support for in-out web services.
 * This is workaround for not working @Transactional + @Resource web services.
 */
public class TransactionHandler implements Handler<MessageContext> {
    private static Log __log = LogFactory.getLog(TransactionHandler.class);
    public static final String TRANSACTION = "org.apache.hise.transaction";
    
    private JpaTransactionManager transactionManager;

    public void setTransactionManager(JpaTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void close(MessageContext arg0) {
        __log.debug("closed");
    }

    public boolean handleFault(MessageContext arg0) {
        __log.debug("handleFault");
        assert Boolean.TRUE.equals(arg0.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY));
        TransactionStatus tx = (TransactionStatus) arg0.get(TRANSACTION); 
        arg0.put(TRANSACTION, null);
        if (tx != null) {
            transactionManager.commit(tx);
        }
        return true;
    }

    public boolean handleMessage(MessageContext arg0) {
        __log.debug("handleMessage outbound:" + arg0.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY));
        
        if (Boolean.FALSE.equals(arg0.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY))) {
            TransactionStatus tx = transactionManager.getTransaction(new DefaultTransactionDefinition());
            assert transactionManager.isValidateExistingTransaction();
            arg0.put(TRANSACTION, tx);
        } else {
            TransactionStatus tx = (TransactionStatus) arg0.get(TRANSACTION); 
            arg0.put(TRANSACTION, null);
            assert tx != null;
            transactionManager.commit(tx);
        }
        return true;
    }

}
