package org.apache.hise;

import junit.framework.Assert;

import org.apache.hise.runtime.Task;
import org.apache.hise.runtime.TaskEvaluator;
import org.apache.hise.utils.DOMUtils;
import org.junit.Test;

public class TaskTest {
    @Test
    public void testEpr() throws Exception {
        Task t = new MockTask();
        TaskEvaluator e = new TaskEvaluator(t);
        String r = DOMUtils.domToString(e.createEprFromHeader(DOMUtils.parse(getClass().getResourceAsStream("/epr.xml")).getDocumentElement()));
        System.out.println(r);
        Assert.assertTrue(r.contains("<wsa:EndpointReference xmlns:wsa=\"http://www.w3.org/2005/08/addressing\"><wsa:Address xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">http://localhost:8082/ClaimsResponseService/</wsa:Address></wsa:EndpointReference>"));
    }
}
