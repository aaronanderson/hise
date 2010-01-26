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

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.hise.engine.store.CompileException;
import org.apache.hise.engine.store.HumanInteractionsCompiler;
import org.apache.hise.lang.HumanInteractions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;


public class CompilerTest {
    @Test
    public void testCompile() throws Exception {
        Resource htdXml = new ClassPathResource("testHtd1.xml");
        HumanInteractions hi = HumanInteractionsCompiler.compile(htdXml);
        Set<QName> s = hi.getTaskDefinitions().keySet();
        Assert.assertNotNull(hi);
        Assert.assertTrue(s.contains(QName.valueOf("{http://www.insurance.example.com/claims}Task1")));
        Assert.assertTrue(s.contains(QName.valueOf("{http://www.insurance.example.com/claims}Task2")));
        Assert.assertTrue(s.contains(QName.valueOf("{http://www.insurance.example.com/claims}Task3")));
        Assert.assertTrue(s.contains(QName.valueOf("{http://www.insurance.example.com/claims}Notify2")));
        Assert.assertEquals("someOutput", hi.getTaskDefinitions().get(QName.valueOf("{http://www.insurance.example.com/claims}Task1")).getOutcomeExpression());
        Assert.assertEquals("approve", hi.getTaskDefinitions().get(QName.valueOf("{http://www.insurance.example.com/claims}Task1")).getTaskInterface().getOperation());
        Assert.assertEquals("notify", hi.getTaskDefinitions().get(QName.valueOf("{http://www.insurance.example.com/claims}Notify2")).getTaskInterface().getOperation());
    }
    
    @Test
    public void testDuplicateTaskDef() throws Exception {
        Resource htdXml = new ClassPathResource("duplicateTaskDef.xml");
        try {
            HumanInteractions hi = HumanInteractionsCompiler.compile(htdXml);
        } catch (CompileException e) {
            Assert.assertTrue(e.getCause().getMessage().contains("Duplicate task found, name: {http://www.insurance.example.com/claims}Task1"));
            return;
        }
        Assert.assertTrue(false);
    }

}
