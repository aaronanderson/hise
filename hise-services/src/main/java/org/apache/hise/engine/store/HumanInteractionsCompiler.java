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

package org.apache.hise.engine.store;

import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.lang.HumanInteractions;
import org.apache.hise.lang.TaskDefinition;
import org.apache.hise.lang.xsd.htd.TGenericHumanRole;
import org.apache.hise.lang.xsd.htd.THumanInteractions;
import org.apache.hise.lang.xsd.htd.TTask;
import org.apache.hise.utils.DOMUtils;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;

public class HumanInteractionsCompiler {
    private static final Log log = LogFactory.getLog(HumanInteractionsCompiler.class);

    private Map<String, String> xmlNamespaces;

    private HumanInteractionsCompiler() {
    }

    public static HumanInteractions compile(Resource resource) throws CompileException {
        Validate.notNull(resource, "Specified resource is null");
        try {
            HumanInteractionsCompiler c = new HumanInteractionsCompiler();
            log.debug("compiling " + resource);
            return c.compile2(resource);
        } catch (Exception e) {
            throw new CompileException("Compile error for " + resource, e);
        }
    }

    private HumanInteractions compile2(Resource resource) throws Exception {
        Validate.notNull(resource);

        Resource htdXml = resource;
        THumanInteractions hiDoc;
        {
            JAXBContext jaxbContext = JAXBContext.newInstance("org.apache.hise.lang.xsd.htd");
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            DocumentBuilderFactory f = DOMUtils.getDocumentBuilderFactory();
            f.setNamespaceAware(true);
            DocumentBuilder b = f.newDocumentBuilder();
            Document d = b.parse(htdXml.getInputStream());
            hiDoc = ((JAXBElement<THumanInteractions>) unmarshaller.unmarshal(d)).getValue();
        }

        HumanInteractions humanInteractions = new HumanInteractions();

        for (TTask tTask : hiDoc.getTasks().getTask()) {
            TaskDefinition taskDefinition = new TaskDefinition(tTask, this.xmlNamespaces, hiDoc.getTargetNamespace());
            compileTaskDef(taskDefinition);
            
            QName name = taskDefinition.getTaskName();
            if (humanInteractions.getTaskDefinitions().containsKey(name)) {
                throw new RuntimeException("Duplicate task found, name: " + name + " resource: " + resource);
            }
            humanInteractions.getTaskDefinitions().put(name, taskDefinition);
        }

        return humanInteractions;
    }

    void compileTaskDef(TaskDefinition t) {
        for (JAXBElement<TGenericHumanRole> e: t.gettTask().getPeopleAssignments().getGenericHumanRole()) {
            log.debug(e);
        }
    }
    
    // /**
    // * Creates HumanInteractions instance, passing DOM Document instance to its constructor.
    // *
    // * @param htdXml
    // * @return
    // * @throws IOException
    // * @throws ParserConfigurationException
    // * @throws SAXException
    // */
    // private HumanInteractions createHumanIteractionsInstance(Resource htdXml) throws Exception {
    // InputStream is;
    //
    // // dom
    // is = htdXml.getInputStream();
    //
    // DocumentBuilderFactory factory = DOMUtils.getDocumentBuilderFactory();
    // factory.setNamespaceAware(true);
    //
    // DocumentBuilder builder = factory.newDocumentBuilder();
    // Document document = builder.parse(is);
    //
    // return new HumanInteractions(document, this.peopleQuery);
    // return null;
    // }
    //
    // /**
    // * Just a very simple, stub org.apache.hise.model.spec.PeopleQuery implementation, which simply creates Assignee instances with a given name.
    // *
    // * @param logicalPeopleGroupName
    // * the logical people group name
    // * @param input
    // * the input message that created the task
    // * @return collection of assignees.
    // */
    // public List<Assignee> evaluate(String logicalPeopleGroupName, Map<String, Message> input) {
    // List<Assignee> assignees = new ArrayList<Assignee>();
    // Group group = new Group();
    // group.setName(logicalPeopleGroupName);
    // assignees.add(group);
    // return assignees;
    // }

    // public QName getTaskName(QName portType, String operation) {
    // return engine.tasksMap.get(HISEEngine.tasksKey(portType, operation));
    // }

}
