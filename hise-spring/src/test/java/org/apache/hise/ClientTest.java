package org.apache.hise;

import java.net.URL;
import javax.xml.namespace.QName;
import junit.framework.Assert;

import org.apache.hise.engine.jaxws.HISEJaxWSClient;
import org.apache.hise.utils.DOMUtils;
import org.junit.Test;

public class ClientTest {
    @Test
    public void testEpr() throws Exception {
        HISEJaxWSClient c = new HISEJaxWSClient();
        c.setServiceName(new QName("http://www.insurance.example.com/claims","ClaimsHandlingService"));
        c.setWsdlDocumentLocation(getClass().getResource("/ExampleTasks.wsdl"));
        c.init();
        Assert.assertEquals("http://localhost:8082/ClaimsResponseService/", c.getAddressFromEpr(DOMUtils.parse(getClass().getResourceAsStream("/epr2.xml")).getDocumentElement()));
    }
}
