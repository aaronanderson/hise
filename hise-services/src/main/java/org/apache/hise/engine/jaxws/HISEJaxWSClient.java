package org.apache.hise.engine.jaxws;

import java.net.URL;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Service;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.hise.utils.XQueryEvaluator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class HISEJaxWSClient {
    private static Log __log = LogFactory.getLog(HISEJaxWSClient.class);
    
    private MessageFactory messageFactory;
    
    URL wsdlDocumentLocation;
    QName serviceName;
    
    private Service destinationService;
    private QName destinationPort;
    private XQueryEvaluator evaluator = new XQueryEvaluator();
    
    public void init() throws Exception {
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
        return (String) evaluator.evaluateExpression("declare namespace wsa=\"http://www.w3.org/2005/08/addressing\"; string(wsa:EndpointReference/wsa:Address)", epr).get(0);
    }

    public Node invoke(Node message, Node epr) {
        try {            
            String address = getAddressFromEpr(epr);
            
            Dispatch<SOAPMessage> dispatch = destinationService.createDispatch(destinationPort, SOAPMessage.class, Service.Mode.MESSAGE);
            __log.debug("sending to address " + address);
            dispatch.getRequestContext().put(Dispatch.ENDPOINT_ADDRESS_PROPERTY, address);
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
