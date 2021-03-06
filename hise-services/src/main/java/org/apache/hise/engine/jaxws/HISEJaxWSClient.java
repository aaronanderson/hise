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

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.api.Sender;
import org.apache.hise.utils.XQueryEvaluator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import java.net.URL;
import java.util.Iterator;
import javax.annotation.PostConstruct;
import org.apache.hise.dao.Transactional;


public class HISEJaxWSClient implements Sender {

    private Log __log = LogFactory.getLog(HISEJaxWSClient.class);
    
    private MessageFactory messageFactory;
    
    URL wsdlDocumentLocation;
    QName serviceName;
    
    private Service destinationService;
    private QName destinationPort;
    private XQueryEvaluator evaluator = new XQueryEvaluator();

    @PostConstruct
    public void init() throws Exception {
        evaluator.declareNamespace("wsa", "http://www.w3.org/2005/08/addressing");
        messageFactory = MessageFactory.newInstance();
        destinationService = Service.create(wsdlDocumentLocation, serviceName);
        destinationPort = null;
        {
            Iterator<QName> it = destinationService.getPorts();
            while (it.hasNext()) {
                QName p = it.next();
                System.out.println(p);
                destinationPort = p;
            }
        }
        Validate.notNull(destinationPort, "Can't find port for service " + serviceName + " in " + wsdlDocumentLocation);
    }
    
    public void setWsdlDocumentLocation(URL wsdlDocumentLocation) {
        this.wsdlDocumentLocation = wsdlDocumentLocation;
    }

    public void setServiceName(QName serviceName) {
        this.serviceName = serviceName;
    }

    public String getAddressFromEpr(Node epr) {
        return (String) evaluator.evaluateExpression("string(wsa:EndpointReference/wsa:Address)", epr).get(0);
    }

    @Transactional
    public Node invoke(Node message, Node epr) {
        try {            
            
            Dispatch<SOAPMessage> dispatch = destinationService.createDispatch(destinationPort, SOAPMessage.class, Service.Mode.MESSAGE);
            
            String address = getAddressFromEpr(epr);
            if (!address.equals("")) {
                __log.debug("sending to address " + address);
                dispatch.getRequestContext().put(Dispatch.ENDPOINT_ADDRESS_PROPERTY, address);
            }
            
            SOAPMessage m;
            m = messageFactory.createMessage();
            Document doc = m.getSOAPBody().getOwnerDocument();
            m.getSOAPBody().appendChild(doc.importNode(message, true));
            return dispatch.invoke(m).getSOAPBody();
        } catch (SOAPException e) {
            throw new RuntimeException(e);
        }
    }

}
