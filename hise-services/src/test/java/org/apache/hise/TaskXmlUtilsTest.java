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

package org.apache.hise;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathConstants;

import net.sf.saxon.dom.NodeWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.dao.Message;
import org.apache.hise.utils.DOMUtils;
import org.apache.hise.utils.TaskXmlUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Node;

@Ignore
public class TaskXmlUtilsTest {
    private static Log __log = LogFactory.getLog(TaskXmlUtilsTest.class);

//    @Test
//    public void testEvaluateXPath() {
//        
//        String xmlRequest = "<enterOrder xmlns:sch='http://www.hise/hise/schema' orderNumber='O26195' caseNumber='C81794' caseType='1' suggestedOwner='1' submitter='1' source='1' issueDate='1' priority='1' note='Niesłychanie pilne. Proszę się pośpieszyć.'>" +
//                            "    <sch:correctiveInvoice customerId='1' customerCode='KLIENT_27959' correctedInvoiceNumber='1' correctionAmount='353.78' issueReason='1'>" +
//                            "        <sch:correctiveInvoiceItem name='Usługi telekomunikacyjne.' newNetValue='424.68' newVat='93.4296' newVatRate='22'/>" +
//                            "        <sch:correctiveInvoiceItem name='Usługi telekomunikacyjne.' newNetValue='1' newVat='0.22' newVatRate='22'/>" +
//                            "    </sch:correctiveInvoice>" +
//                            "</enterOrder>";
//
//        Map<String, Message> input = new HashMap<String, Message>();
//        input.put("enterOrder", new Message(xmlRequest));
//        
//        TaskXmlUtils txu = new TaskXmlUtils(new NamespaceContext() {
//
//            public String getNamespaceURI(String prefix) {
//                
//                if (prefix.equals("hise")) {
//                    return "http://www.hise/hise/schema";
//                }
//                if (prefix.equals("htd")) {
//                    return "http://www.example.org/WS-HT";
//                }
//                
//                return null;
//            }
//
//            public String getPrefix(String namespaceURI) {
//                return null;
//            }
//
//            public Iterator getPrefixes(String namespaceURI) {
//                return null;
//            }
//            
//        }, input, null);
//        
////      Object o = txu.evaluateXPath("htd:getInput(\"enterOrder\")/hise:correctiveInvoice/@customerId", XPathConstants.STRING);
////        Object o = txu.evaluateXPath("htd:getInput(\"enterOrder\")/hise:correctiveInvoice/hise:correctiveInvoiceItem/@name", XPathConstants.NODE);
//        Object o = txu.evaluateXPath("htd:getInput(\"enterOrder\")/hise:correctiveInvoice/@correctedInvoiceNumber", XPathConstants.STRING);
//        //__log.debug(DOMUtils.domToString((Node) o));
//        
//        assertNotNull(o);
//        assertEquals("1", o);
//    }
    
}
