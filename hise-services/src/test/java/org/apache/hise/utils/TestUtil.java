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

package org.apache.hise.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.hise.api.HumanInteractionsManager;
import org.apache.hise.api.PeopleQuery;
import org.apache.hise.dao.Assignee;
import org.apache.hise.engine.HumanInteractionsCompiler;
import org.apache.hise.lang.faults.HTException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;


/*
 * TODO: ADD SHORT DESCRIPTION 
 *
 * @author <a href="mailto:jkr@touk.pl">Jakub Kurlenda</a>
 */
@org.junit.Ignore
public class TestUtil {
    
    /**
     * Helper method for creating HumanInteractionsManager instance from a given set of definition files.
     *
     * @param htdFiles - files, which contain human interactions definitions.
     * @return
     * @throws HTException
     */
    public static HumanInteractionsManager createHumanInteractionsManager(String... htdFiles) throws HTException {
        
        List<Resource> resources = new ArrayList<Resource>();
        
        for (String htdFile : htdFiles) {
            resources.add(new ClassPathResource(htdFile));
        }
        
        return new HumanInteractionsCompiler((Resource[]) resources.toArray(), new PeopleQuery() {

            public List<Assignee> evaluate(String logicalPeopleGroupName, Map<String, Object> parameters) {
                return new ArrayList<Assignee>();
            }
            
        }, null);
    }
}
