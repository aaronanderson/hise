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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.api.PeopleQuery;
import org.apache.hise.dao.Assignee;
import org.apache.hise.dao.Group;
import org.apache.hise.dao.Message;
import org.apache.hise.lang.HumanInteractions;
import org.apache.hise.lang.TaskDefinition;
import org.apache.hise.lang.faults.HTConfigurationException;
import org.apache.hise.lang.faults.HTException;
import org.apache.hise.utils.DOMUtils;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.apache.hise.lang.xsd.htd.THumanInteractions;
import org.apache.hise.lang.xsd.htd.TTask;

/* 
 * Human interactions manager.
 * 
 * @author <a href="mailto:jkr@touk.pl">Jakub Kurlenda</a>
 * @author <a href="mailto:ww@touk.pl">Witek Wołejszo</a>
 */
@Service
public class HumanInteractionsCompiler {

    private final Log log = LogFactory.getLog(HumanInteractionsCompiler.class);
    
    private HISEEngine engine;

    // ============= FIELDS ===================
    
    /**
     * XML namespaces supported in human task definitions.
     */
    private Map<String, String> xmlNamespaces;
    
    private PropertyBasedPeopleQuery peopleQuery;
    
    // ============= CONSTRUCTOR ===================

    /**
     * Creates HumanInteractionsManagerImpl, with a given human interactions definition list. Provides
     * default {@link PeopleQuery} implementation which returns empty result set.
     *
     * @param resources collection of *.xml files with human interactions definitions.
     * @throws HTException thrown when task definition names are not unique 
     */
    public HumanInteractionsCompiler(Resource resource, PropertyBasedPeopleQuery peopleQuery, Map<String, String> xmlNamespaces) throws HTException {
        
        Validate.notNull(resource);
        
        this.peopleQuery = peopleQuery;
        this.xmlNamespaces = xmlNamespaces;
        
        Map<String, String> taskDefinitionsNamesMap = new HashMap<String, String>();
        
        Resource htdXml = resource;
            
            try {
                
                // obtain HumanInteractions instance from a given resource.
                HumanInteractions humanInteractions = createHumanIteractionsInstance(htdXml);
                
                // Extract JAXB model from a given resource.
                THumanInteractions hiDoc = unmarshallHumanInteractionsData(htdXml);

                // Fetch all task definitions inside JAXB model
                List<TaskDefinition> taskDefinitions = extractTaskDefinitionList(hiDoc, humanInteractions, taskDefinitionsNamesMap);
                 
                humanInteractions.setTaskDefinitions(taskDefinitions);

                for (TaskDefinition d : taskDefinitions) {
                    String key = HISEEngine.tasksKey(d.getInterface().getPortType(), d.getInterface().getOperation());
                    assert(engine.tasksMap.get(key) == null);
                    log.debug("registering route " + key + " -> " + d.getTaskName());
                    engine.tasksMap.put(key, d.getTaskName());
                }
                
            } catch (Exception e) {
                throw new HTConfigurationException("Error parsing configuration.", e);
            }
    }
    
    /**
     * Default scope constructor used in Unit Tests.
     */
    HumanInteractionsCompiler() {
        
    }


    // ============= INTERFACE IMPLEMENTATION  ===================

    /*
     * Retrieves task definition by name.
     *
     * @param taskName - sought task definition name.
     * @return TaskDefinition instance.
     * @throws HumanTaskException in case when no such task definition was found.
     */

    public TaskDefinition getTaskDefinition(QName taskName) {
        Validate.notNull(taskName);
        return engine.getTaskDefinition(taskName);
    }

    // ============= HELPER METHODS ===================
    
    /**
     * Checks, if a task with a given name already exists in the context. 
     *
     * @param taskName
     * @throws HTException if task with a given name already exists.
     */
    private void checkTaskDefinitionUniqueness(String taskName, Map<String, String> taskDefinitionNamesMap) throws HTException {
        if (taskDefinitionNamesMap.containsKey(taskName)) {
            throw new HTException("Task definition names must be unique!");
        }
    }

    /**
     * Extracts TaskDefinition object instances from JAXB model.
     *  
     * @param hiDoc JAXB model containing task definition data.
     * @param humanInteractions parent for a given task definition, which contains all the data from the xml file. 
     * @return list of TaskDefinition instances.
     * @throws HTException if task with a given name already exists.
     */
    private List<TaskDefinition> extractTaskDefinitionList(THumanInteractions hiDoc, HumanInteractions humanInteractions, Map<String, String> taskDefinitionNamesMap) throws HTException {
        List<TaskDefinition> taskDefinitions = new ArrayList<TaskDefinition>();

        for (TTask tTask : hiDoc.getTasks().getTask()) {
            checkTaskDefinitionUniqueness(tTask.getName(), taskDefinitionNamesMap);
            TaskDefinition taskDefinition = new TaskDefinition(tTask, this.peopleQuery, this.xmlNamespaces, hiDoc.getTargetNamespace());
            //taskDefinition.setDefinition(humanInteractions);
            taskDefinitions.add(taskDefinition);
            taskDefinitionNamesMap.put(tTask.getName(), "XXX");
        }
        return taskDefinitions;
    }

    /**
     * Unmarshals human interactions data from the XML file.
     * @param htdXml the Resource containing human interactions definition
     * @return
     * @throws JAXBException
     * @throws IOException
     */
    protected THumanInteractions unmarshallHumanInteractionsData(Resource htdXml) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance("org.apache.hise.lang.xsd.htd");
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        DocumentBuilderFactory f = DOMUtils.getDocumentBuilderFactory();
        f.setNamespaceAware(true);
        DocumentBuilder b = f.newDocumentBuilder();
        Document d = b.parse(htdXml.getInputStream());
        return ((JAXBElement<THumanInteractions>) unmarshaller.unmarshal(d)).getValue();
    }

    /**
     * Creates HumanInteractions instance, passing DOM Document instance to its constructor.
     * 
     * @param htdXml
     * @return
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    private HumanInteractions createHumanIteractionsInstance(Resource htdXml) throws IOException, ParserConfigurationException, SAXException {
        InputStream is;

        // dom
        is = htdXml.getInputStream();

        DocumentBuilderFactory factory = DOMUtils.getDocumentBuilderFactory();
        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(is);

        return new HumanInteractions(document, this.peopleQuery);
    }

    /**
     * Just a very simple, stub org.apache.hise.model.spec.PeopleQuery implementation, which simply creates Assignee instances with a given name.
     *
     * @param logicalPeopleGroupName the logical people group name
     * @param input                  the input message that created the task
     * @return collection of assignees.
     */
    public List<Assignee> evaluate(String logicalPeopleGroupName, Map<String, Message> input) {
        List<Assignee> assignees = new ArrayList<Assignee>();
        Group group = new Group();
        group.setName(logicalPeopleGroupName);
        assignees.add(group);
        return assignees;
    }

    public QName getTaskName(QName portType, String operation) {
        return engine.tasksMap.get(HISEEngine.tasksKey(portType, operation));
    }

}
