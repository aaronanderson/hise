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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.api.PeopleQuery;
import org.apache.hise.dao.Assignee;
import org.apache.hise.dao.Person;
import org.springframework.core.io.Resource;



/**
 * Loads property file. Returns all people listed in property file together with
 * users specified literally.
 *
 * @author Witek Wołejszo
 * @author Mateusz Lipczyński
 */
public final class PropertyBasedPeopleQuery implements PeopleQuery {

    private final Log log = LogFactory.getLog(PropertyBasedPeopleQuery.class);

    private Resource configuration;
    
    /**
     * 
     * @param logicalPeopleGroupName
     * @param parameters
     * @return
     */
    public List<Assignee> evaluate(String logicalPeopleGroupName, Map<String, Object> parameters) {
        
        log.info("Evaluating members of logical people group: " + logicalPeopleGroupName);
        
        List<Assignee> result = new ArrayList<Assignee>();
        Properties p = new Properties();
        
        try {
            
            p.load(this.configuration.getInputStream());
            String value = (String) p.get(logicalPeopleGroupName);
            // parse
            String[] peopleInGroup = value.split(",");
            for (String name : peopleInGroup) {
                result.add(new Person(name));
            }
            
        } catch (IOException e) {
            
            try {
                
                log.error("Error reading: " + this.configuration.getURL());
            
            } catch (IOException e1) {

                //Access error should not affect evaluation TODO: ref to specs

            } finally {
                
                log.error("Error reading file.", e);
            }
        }
        return result;
    }

    public void setConfiguration(Resource configuration) {
        this.configuration = configuration;
    }

    public Resource getConfiguration() {
        return this.configuration;
    }

}
