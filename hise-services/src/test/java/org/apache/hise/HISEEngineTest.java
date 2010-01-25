package org.apache.hise;

import junit.framework.Assert;

import org.apache.hise.engine.HISEEngine;
import org.apache.hise.utils.DOMUtils;
import org.junit.Test;

public class HISEEngineTest {
    @Test
    public void testEval2() throws Exception {
        String r = HISEEngine.fetchCreatedBy(DOMUtils.parse(getClass().getResourceAsStream("/approveHeader.xml")));
        Assert.assertEquals("soapui", r);
    }
}
