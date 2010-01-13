package org.apache.hise;

import junit.framework.Assert;

import org.apache.hise.engine.jaxws.HISEJaxWSClient;
import org.apache.hise.utils.DOMUtils;
import org.junit.Test;

public class ClientTest {
    @Test
    public void testEpr() throws Exception {
        HISEJaxWSClient c = new HISEJaxWSClient();
        Assert.assertEquals("http://localhost:8082/ClaimsResponseService/", c.getAddressFromEpr(DOMUtils.parse(getClass().getResourceAsStream("/epr2.xml")).getDocumentElement()));
    }
}
