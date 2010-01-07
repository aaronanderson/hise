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

import java.util.HashMap;
import java.util.Map;

import org.apache.hise.utils.RegexpTemplateEngine;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests for {@link RegexpTemplateEngine}.
 * @author Witek Wołejszo
 */
public class RegexpTemplateEngineTest {

    @Test
    public void mergeTest1() {
        
        RegexpTemplateEngine te = new RegexpTemplateEngine();
        String r1 = te.merge("Raz dwa trzy.", null);
        
        Assert.assertEquals("Raz dwa trzy.", r1);
        
        Map<String, Object> pp = new HashMap<String, Object>();
        
        pp.put("Raz", "1");
        String r2 = te.merge("$Raz$ dwa trzy.", pp);
        Assert.assertEquals("1 dwa trzy.", r2);
        
        pp.put("dwa", "2");
        String r3 = te.merge("$Raz$ $dwa$ trzy.", pp);
        Assert.assertEquals("1 2 trzy.", r3);
        
        pp.put("trzy", "3");
        String r4 = te.merge("$Raz$ $dwa$ $trzy$.", pp);
        Assert.assertEquals("1 2 3.", r4);
    }
    
    @Test
    public void mergeTest2() {
        
        RegexpTemplateEngine te = new RegexpTemplateEngine();
        
        Map<String, Object> pp = new HashMap<String, Object>();
        pp.put("euroAmount", Double.valueOf(1));
        pp.put("firstname", "jan");
        pp.put("lastname", "kowalski");
        
        String r1 = te.merge("Approve the insurance claim for €$euroAmount$ on behalf of $firstname$ $lastname$", pp);
        Assert.assertEquals("Approve the insurance claim for €1.0 on behalf of jan kowalski", r1);
    }
    
    @Test
    public void removeTest1() {
        RegexpTemplateEngine te = new RegexpTemplateEngine();
        Map<String, Object> pp = new HashMap<String, Object>();
        //no x in pp
        String r1 = te.merge("?IF-x?bla bla bla?ENDIF-x?", pp);
        Assert.assertEquals("", r1);
    }
    
    @Test
    public void noRemoveTest1() {
        RegexpTemplateEngine te = new RegexpTemplateEngine();
        Map<String, Object> pp = new HashMap<String, Object>();
        pp.put("x", "1");
        pp.put("y", "bleh");
        String r1 = te.merge("?IF-x?bla $y$ bla?ENDIF-x?", pp);
        Assert.assertEquals("bla bleh bla", r1);
    }
    
    @Test
    public void noRemoveTest2() {
        RegexpTemplateEngine te = new RegexpTemplateEngine();
        Map<String, Object> pp = new HashMap<String, Object>();
        pp.put("correctedItemName1", "pozycja");
        pp.put("correctedItemNewNetValue1", "1");
        String r1 = te.merge("?IF-correctedItemName1?Pozycja: $correctedItemName1$ Na: $correctedItemNewNetValue1$?ENDIF-correctedItemName1?", pp);
        Assert.assertEquals("Pozycja: pozycja Na: 1", r1);
    }
    
    @Test
    public void noRemoveTest3() {
        RegexpTemplateEngine te = new RegexpTemplateEngine();
        Map<String, Object> pp = new HashMap<String, Object>();
        pp.put("correctedItemName1", "pozycja");
        pp.put("correctedItemNewNetValue1", "1");
        pp.put("correctedItemName2", null);
        pp.put("correctedItemNewNetValue2", null);
        String r1 = te.merge("?IF-correctedItemName1?Pozycja: $correctedItemName1$ Na: $correctedItemNewNetValue1$?ENDIF-correctedItemName1??IF-correctedItemName2?Pozycja: $correctedItemName2$ Na: $correctedItemNewNetValue2$?ENDIF-correctedItemName2?", pp);
        Assert.assertEquals("Pozycja: pozycja Na: 1", r1);
    }
    
    @Test
    public void combinedTest1() {
        RegexpTemplateEngine te = new RegexpTemplateEngine();
        Map<String, Object> pp = new HashMap<String, Object>();
        pp.put("y", "1");
        String r1 = te.merge("?IF-x?bla bla bla?ENDIF-x?$y$", pp);
        Assert.assertEquals("1", r1);
    }
    
    @Test
    public void mergeTestNoPresentationValue() {
        
        RegexpTemplateEngine te = new RegexpTemplateEngine();
        
        Map<String, Object> pp = new HashMap<String, Object>();
        String r1 = te.merge("$Raz$ dwa $trzy$.", pp);
        
        Assert.assertEquals("error:Raz dwa error:trzy.", r1);
    }
    
}
