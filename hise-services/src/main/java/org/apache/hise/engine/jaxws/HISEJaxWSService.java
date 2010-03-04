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

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
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
import org.apache.hise.api.HISEEngine;
import org.apache.hise.api.Handler;
import org.apache.hise.engine.HISEEngineImpl;
import org.springframework.orm.jpa.JpaCallback;
import org.springframework.orm.jpa.JpaTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@WebServiceProvider
@ServiceMode(value = Service.Mode.MESSAGE)
public class HISEJaxWSService implements Provider<SOAPMessage>, Handler {
    private static Log __log = LogFactory.getLog(HISEJaxWSService.class);

    private HISEEngine hiseEngine;
    private WebServiceContext context;
    private PlatformTransactionManager transactionManager;
    private MessageFactory messageFactory;
    private TransactionTemplate transactionTemplate;
    private String id;

    public HISEJaxWSService() throws Exception {
        messageFactory = MessageFactory.newInstance();
    }

    public void init() {
        transactionTemplate = new TransactionTemplate(transactionManager);
    }
    
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setHiseEngine(HISEEngine hiseEngine) {
        this.hiseEngine = hiseEngine;
    }

    public WebServiceContext getContext() {
        return context;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Resource
    public void setContext(WebServiceContext context) {
        this.context = context;
    }

    public SOAPMessage invoke(final SOAPMessage request) {
        return (SOAPMessage) transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus arg0) {
                try {
                    // TransactionStatus tx = transactionManager.getTransaction(new DefaultTransactionDefinition());
//                    assert transactionManager.isValidateExistingTransaction();
                    MessageContext c = context.getMessageContext();
                    Object operationInfo = c.get("org.apache.cxf.service.model.OperationInfo");
                    QName operation = (QName) operationInfo.getClass().getMethod("getName").invoke(operationInfo);
                    QName portType = (QName) c.get("javax.xml.ws.wsdl.interface");
                    QName operation2 = (QName) c.get("javax.xml.ws.wsdl.operation");

                    Element body = request.getSOAPBody();
                    __log.debug("invoking " + request + " operation:" + operation + " portType:" + portType + " operation2:" + operation2);
                    Node approveResponseHeader = hiseEngine.receive(HISEJaxWSService.this, portType, operation.getLocalPart(), body, request.getSOAPHeader());
                    SOAPMessage m = messageFactory.createMessage();
                    
                    Document doc = m.getSOAPHeader().getOwnerDocument();
                    if (approveResponseHeader != null) {
                        m.getSOAPHeader().appendChild(doc.importNode(approveResponseHeader, true));
                    }
                    return m;
                } catch (Exception e) {
                    throw new RuntimeException("Error during receiving message ", e);
                }
            }
        });
    }
}
