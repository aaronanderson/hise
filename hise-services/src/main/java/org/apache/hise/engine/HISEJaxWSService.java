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

package org.apache.hise.engine;

import javax.annotation.Resource;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.api.HumanTaskServices;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.w3c.dom.Element;



@WebServiceProvider
@ServiceMode(value=Service.Mode.MESSAGE)
public class HISEJaxWSService implements Provider<SOAPMessage> {
    private static Log __log = LogFactory.getLog(HISEJaxWSService.class);
    
    private HumanTaskServices services;
    private WebServiceContext context;
    private JpaTransactionManager transactionManager;
    private MessageFactory messageFactory;

    public HISEJaxWSService() throws Exception {
        messageFactory = MessageFactory.newInstance();
    }
    
    public WebServiceContext getContext() {
        return context;
    }

    public void setTransactionManager(JpaTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Resource
    public void setContext(WebServiceContext context) {
        this.context = context;
    }

    public HumanTaskServices getServices() {
        return services;
    }

    public void setServices(HumanTaskServices services) {
        this.services = services;
    }

    public SOAPMessage invoke(SOAPMessage request) {
        try {
//            TransactionStatus tx = transactionManager.getTransaction(new DefaultTransactionDefinition());
            assert transactionManager.isValidateExistingTransaction();
            MessageContext c = context.getMessageContext();
            Object operationInfo = c.get("org.apache.cxf.service.model.OperationInfo");
            QName operation = (QName) operationInfo.getClass().getMethod("getName").invoke(operationInfo);
            QName portType = (QName) c.get("javax.xml.ws.wsdl.interface");
            QName operation2 = (QName) c.get("javax.xml.ws.wsdl.operation");
            
            Element body = request.getSOAPBody();
            __log.debug("invoking " + request + " operation:" + operation + " portType:" + portType + " operation2:" + operation2);
            services.receive(portType, operation.getLocalPart(), body, context.getUserPrincipal().getName());
            SOAPMessage m = messageFactory.createMessage();
//            transactionManager.commit(tx);
            return m;
        } catch (Exception e) {
            __log.debug("", e);
            return null;
        }
    }
}
