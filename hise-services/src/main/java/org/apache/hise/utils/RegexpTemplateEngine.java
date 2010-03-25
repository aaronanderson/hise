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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Utility class used to merge presentation parameters into template strings using regexp replace.
 * @author Witek Wołejszo
 */
public class RegexpTemplateEngine implements TemplateEngine {
    
    private final Log log = LogFactory.getLog(RegexpTemplateEngine.class);

    /* (non-Javadoc)
     * @see org.apache.hise.utils.TemplateEngine#merge(java.lang.String, java.util.Map)
     */
    public String merge(String template, Map<String, Object> presentationParameterValues) {
         
        Pattern blockPattern = Pattern.compile("\\?IF\\-[A-Za-z0-9]*\\?.*\\?ENDIF\\-[A-Za-z0-9]*\\?");
        Matcher m = blockPattern.matcher(template);
        
        //- remove blocks from template if the key is not in presentationParameterValues.keySet or value is null
        //- remove block markers otherwise
        while (m.find() == true) {
            
            String key = m.group().substring(4).replaceAll("\\?.*$", "");

            if (presentationParameterValues.get(key) == null) {
                template = m.replaceFirst("");
            } else {
                template = template.replace("?IF-" + key + "?", "").replace("?ENDIF-" + key + "?", "");
            }
            
            m = blockPattern.matcher(template);
        }

        Pattern replacePattern = Pattern.compile("\\$[A-Za-z0-9]*\\$");
        m = replacePattern.matcher(template);
        
        while (m.find() == true) {
            
            String key = m.group().replace("$", "");
            Object substitution = ((presentationParameterValues == null) ? null : presentationParameterValues.get(key));
            String substitutionString = (substitution == null) ? "error:" + key : substitution.toString();
            
            if (substitutionString == null) {
                
                log.warn("Cannot find presentation parameter: " + key);
                
            } else {

                template = m.replaceFirst(substitutionString);
                m = replacePattern.matcher(template);
            }
        }

        return template;
    }
    
}
