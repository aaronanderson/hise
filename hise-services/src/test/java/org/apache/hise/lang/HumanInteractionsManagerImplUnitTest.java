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

package org.apache.hise.lang;

import static org.junit.Assert.assertNotNull;

import javax.xml.namespace.QName;

import org.apache.hise.api.HumanInteractionsManager;
import org.apache.hise.engine.HumanInteractionsCompiler;
import org.apache.hise.lang.TaskDefinition;
import org.apache.hise.lang.faults.HTConfigurationException;
import org.apache.hise.lang.faults.HTException;
import org.apache.hise.utils.TestUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


/**
 * {@link HumanInteractionsCompiler} class unit tests.
 *
 * @author <a href="mailto:jkr@touk.pl">Jakub Kurlenda</a>
 */
@Ignore
public class HumanInteractionsManagerImplUnitTest {

    private HumanInteractionsManager taskManager;

    @Before
    public void setUp() throws Exception {

        this.taskManager = TestUtil.createHumanInteractionsManager("testHtd1-human-interaction.xml");
    }

    @Test
    public void testGetTaskDefinitionByName() throws HTException {
        TaskDefinition taskDefinition = taskManager.getTaskDefinition(new QName("http://www.insurance.example.com/claims/", "Task1"));
        assertNotNull(taskDefinition);
    }

    @Test(expected = HTConfigurationException.class)
    public void testTaskDefinitionNotFound() {
        taskManager.getTaskDefinition(new QName("http://www.insurance.example.com/claims/", "JKR"));
    }

    /**
     * Sprawdzenie, czy w przypadku duplikacji nazwy tasku konstruktor managera wyrzuci wyjątek.
     *
     * @throws HTException
     */
    @Test(expected = HTException.class)
    public void testNonUniqueTaskDefinitionsNameException() throws HTException {
        TestUtil.createHumanInteractionsManager("testHtdDuplicateTaskDefinition.xml");
    }
}