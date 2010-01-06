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

package org.apache.hise.engine;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.hise.engine.HumanInteractionsCompiler;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;


public class HumanInteractionsManagerImplTest {

    //private final Log log = LogFactory.getLog(HumanInteractionsManagerImplTest.class);
    
    @Test
    public void testUnmarshallHumanInteractionsData() throws Exception {
        Resource htdXml = new ClassPathResource("testHtd1-human-interaction.xml");
        HumanInteractionsCompiler humanInteractionsManagerImpl = new HumanInteractionsCompiler();
        org.apache.hise.lang.xsd.htd.THumanInteractions hi = humanInteractionsManagerImpl.unmarshallHumanInteractionsData(htdXml);
        Assert.assertNotNull(hi);
    }

}
