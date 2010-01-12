package org.apache.hise;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

import org.apache.commons.lang.Validate;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class ClientTest {
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
        dispatch.getRequestContext().put(Dispatch.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:8082/ClaimsHandlingService/");
        SOAPMessage m;
        MessageFactory messageFactory = MessageFactory.newInstance();
        m = messageFactory.createMessage();
        m.getSOAPBody().addTextNode("asdf");
        m.writeTo(System.out);
        dispatch.invoke(m);
    }
}
