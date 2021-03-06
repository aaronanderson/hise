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

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.hise.dao.Message;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


/**
 * {@link Message} tests.
 * @author Witek Wołejszo
 */
@Ignore
public class MessageTest {

//    @Test
//    public void testGetDomDocument() throws ParserConfigurationException, SAXException, IOException {
//        Message message = new Message("<ClaimApprovalRequest><cust><firstname>witek</firstname></cust></ClaimApprovalRequest>");
//        Document doc = message.getDomDocument();
//        assertNotNull(doc);
//    }
//
//    @Test
//    public void testGetRootNodeName() {
//        Message message = new Message("<ClaimApprovalRequest><cust><firstname>witek</firstname></cust></ClaimApprovalRequest>");
//        String r = message.getRootNodeName();
//        assertEquals("ClaimApprovalRequest", r);
//    }
//    
//    @Test
//    public void testGetRootNodeName_XML_WithProcessingInstruction() {
//        Message message = new Message("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ClaimApprovalRequest><cust><firstname>witek</firstname></cust></ClaimApprovalRequest>");
//        String r = message.getRootNodeName();
//        assertEquals("ClaimApprovalRequest", r);
//    }

}
