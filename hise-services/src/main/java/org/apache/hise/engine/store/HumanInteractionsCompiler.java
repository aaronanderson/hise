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

import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.lang.HumanInteractions;
import org.apache.hise.lang.TaskDefinition;
import org.apache.hise.lang.xsd.htd.THumanInteractions;
import org.apache.hise.lang.xsd.htd.TImport;
import org.apache.hise.lang.xsd.htd.TNotification;
import org.apache.hise.lang.xsd.htd.TTask;
import org.apache.hise.lang.xsd.htd.TTaskInterface;

public class HumanInteractionsCompiler {

    private final Log log = LogFactory.getLog(HumanInteractionsCompiler.class);
    private Map<String, String> xmlNamespaces;

    private HumanInteractionsCompiler() {
    }

    public static HumanInteractions compile(URL resource) throws CompileException {
        Validate.notNull(resource, "Specified resource is null");
        try {
            HumanInteractionsCompiler c = new HumanInteractionsCompiler();
            LogFactory.getLog(HumanInteractionsCompiler.class).debug("compiling " + resource);
            return c.compile2(resource);
        } catch (Exception e) {
            throw new CompileException("Compile error for " + resource, e);
        }
    }

    private HumanInteractions compile2(URL resource) throws Exception {
        Validate.notNull(resource);

        URL htdXml = resource;
        THumanInteractions hiDoc;
        {
            JAXBContext jaxbContext = JAXBContext.newInstance("org.apache.hise.lang.xsd.htd");
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            hiDoc = ((JAXBElement<THumanInteractions>) unmarshaller.unmarshal(htdXml.openStream())).getValue();
        }

        Set<Definition> definitions = new HashSet<Definition>();

        for (TImport tImport : hiDoc.getImport()) {
            if ("http://schemas.xmlsoap.org/wsdl/".equals(tImport.getImportType())) {
                try {
                    URI wsdl = new URI(tImport.getLocation());
                    if (!wsdl.isAbsolute()){
                      wsdl = htdXml.toURI().resolve(wsdl);
                    }
                    WSDLFactory wsdlf = WSDLFactory.newInstance();
                    WSDLReader reader = wsdlf.newWSDLReader();
                    Definition definition = reader.readWSDL(wsdl.toString());
                    definitions.add(definition);
                } catch (Exception ex) {
                    log.error("Error during reading wsdl file.", ex);
                }
            }
        }

        HumanInteractions humanInteractions = new HumanInteractions();

        if (hiDoc.getTasks() != null) {
            for (TTask tTask : hiDoc.getTasks().getTask()) {
                TaskDefinition taskDefinition = new TaskDefinition(tTask, this.xmlNamespaces, hiDoc.getTargetNamespace());
                taskDefinition.setTaskInterface(tTask.getInterface());

                QName name = taskDefinition.getTaskName();
                if (humanInteractions.getTaskDefinitions().containsKey(name)) {
                    throw new RuntimeException("Duplicate task found, name: " + name + " resource: " + resource);
                }
                humanInteractions.getTaskDefinitions().put(name, taskDefinition);

                QName portTypeName = taskDefinition.getTaskInterface().getPortType();
                taskDefinition.setPortType(findPortType(portTypeName, definitions));
            }
        }

        if (hiDoc.getNotifications() != null) {
            for (TNotification tnote : hiDoc.getNotifications().getNotification()) {
                TaskDefinition taskDefinition = new TaskDefinition(tnote, this.xmlNamespaces, hiDoc.getTargetNamespace());
                TTaskInterface x = new TTaskInterface();
                x.setOperation(tnote.getInterface().getOperation());
                x.setPortType(tnote.getInterface().getPortType());
                taskDefinition.setTaskInterface(x);

                QName name = taskDefinition.getTaskName();
                if (humanInteractions.getTaskDefinitions().containsKey(name)) {
                    throw new RuntimeException("Duplicate task found, name: " + name + " resource: " + resource);
                }
                humanInteractions.getTaskDefinitions().put(name, taskDefinition);

                QName portTypeName = taskDefinition.getTaskInterface().getPortType();
                taskDefinition.setPortType(findPortType(portTypeName, definitions));
            }
        }

        return humanInteractions;
    }

    private PortType findPortType(QName portTypeName, Set<Definition> definitions) {
        for (Definition definition : definitions) {
            PortType portType = (PortType) definition.getAllPortTypes().get(portTypeName);
            if (portType != null) {
                return portType;
                }
            }

        throw new RuntimeException("PortType not found in definitions portType: " + portTypeName);
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
