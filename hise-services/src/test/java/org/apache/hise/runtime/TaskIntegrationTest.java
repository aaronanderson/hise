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

package org.apache.hise.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.annotation.Resource;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;

import org.apache.hise.api.HumanInteractionsManager;
import org.apache.hise.dao.Message;
import org.apache.hise.lang.faults.HTException;
import org.apache.hise.runtime.Task;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;



/**
 * {@link Task} integration tests. Tests compile time weaving.
 *
 * @author Witek Wołejszo
 */
@ContextConfiguration(locations = "classpath:/test.xml")
@Ignore
public class TaskIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Resource
    HumanInteractionsManager humanInteractionsManager;
    
    public HumanInteractionsManager getHumanInteractionsManager() {
        return humanInteractionsManager;
    }

    public void setHumanInteractionsManager(HumanInteractionsManager humanInteractionsManager) {
        this.humanInteractionsManager = humanInteractionsManager;
    }

    //@Test
    public void testEvaluateXPathGetInput_Namespace() throws HTException {
        
        Assume.assumeNotNull(this.humanInteractionsManager);
        
        String xmlRequest = "<enterOrder xmlns:sch='http://www.hise/hise/schema' orderNumber='O26195' caseNumber='C81794' caseType='1' suggestedOwner='1' submitter='1' source='1' issueDate='1' priority='1' note='Niesłychanie pilne. Proszę się pośpieszyć.'>" +
                            "    <sch:correctiveInvoice customerId='1' customerCode='KLIENT_27959' correctedInvoiceNumber='1' correctionAmount='353.78' issueReason='1'>" +
                            "        <sch:correctiveInvoiceItem name='Usługi telekomunikacyjne.' newNetValue='424.68' newVat='93.4296' newVatRate='22'/>" +
                            "        <sch:correctiveInvoiceItem name='Usługi telekomunikacyjne.' newNetValue='1' newVat='0.22' newVatRate='22'/>" +
                            "    </sch:correctiveInvoice>" +
                            "</enterOrder>";

        Task t = new Task();
        t.setHumanInteractionsManager(humanInteractionsManager);
        t.init(this.humanInteractionsManager.getTaskDefinition(new QName("http://www.insurance.example.com/claims/", "Task1")), null, xmlRequest);
        
        t.getInput().put("enterOrder", new Message(xmlRequest));

        Object o = t.evaluateXPath("htd:getInput(\"enterOrder\")/hise:correctiveInvoice/@customerId", XPathConstants.STRING);

        assertNotNull(o);
        assertEquals("1", o);
    }
}
