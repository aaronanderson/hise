package org.apache.hise;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

import org.apache.commons.lang.Validate;
import org.apache.hise.utils.DOMUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;

//@Ignore
public class Client2Test {
    @Test
    public void testClient() throws Exception {
        Service destinationService = Service.create(getClass().getResource("/ExampleTasks.wsdl"), QName.valueOf("{http://www.insurance.example.com/claims}ClaimsResolvingService"));
        QName destinationPort = null;
        {
            Iterator<QName> it = destinationService.getPorts();
            while (it.hasNext()) {
                QName p = it.next();
                System.out.println(p);
                destinationPort = p;
            }
        }
        Validate.notNull(destinationPort);
        
        Dispatch<SOAPMessage> dispatch;
        dispatch = destinationService.createDispatch(destinationPort, SOAPMessage.class, Service.Mode.MESSAGE);
        dispatch.getRequestContext().put(Dispatch.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:8084/ClaimsResponse/");
        //Tue Jan 12 17:50:56 GMT+01:00 2010:DEBUG:>> "SOAPAction: "http://www.insurance.example.com/claims/approve"[\r][\n]"

        dispatch.getRequestContext().put(Dispatch.SOAPACTION_URI_PROPERTY, "http://www.insurance.example.com/claims/approve");
        SOAPMessage m;
        MessageFactory messageFactory = MessageFactory.newInstance();
        m = messageFactory.createMessage();
        Document response = DOMUtils.parse("<cla:resolve xmlns:cla=\"http://www.insurance.example.com/claims\"><ok>true</ok></cla:resolve>");
        Document doc = m.getSOAPBody().getOwnerDocument();
        m.getSOAPBody().appendChild(doc.importNode(response.getDocumentElement(), true));
        m.writeTo(System.out);
        dispatch.invoke(m);
    }
}
