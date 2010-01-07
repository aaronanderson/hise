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

import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.lang.xsd.htd.TTask;
import org.apache.hise.lang.xsd.htd.TTaskInterface;
import org.apache.hise.utils.DOMUtils;

/**
 * Holds information about task version runnable in TouK Human Task engine. Task
 * consists of:
 * <p/>
 * - HumanInteractions object - task name (reference to single task in HumanInteractions object)
 *
 * @author Witek Wołejszo
 * @author Kamil Eisenbart
 * @author Mateusz Lipczyński
 */
public class TaskDefinition {

    private static final Log log = LogFactory.getLog(TaskDefinition.class);

    private final TTask tTask;

    private String targetNamespace; 
    
    /**
     * XML namespaces supported in human task definitions.
     */
    private Map<String, String> xmlNamespaces;

    // ==================== CONSTRUCTOR =========================

    public TaskDefinition(TTask taskDefinition, Map<String, String> xmlNamespaces, String targetNamespace) {

        super();

        Validate.notNull(taskDefinition);

        this.tTask = taskDefinition;
        this.xmlNamespaces = xmlNamespaces;
        this.targetNamespace = targetNamespace;
    }

    
    public TTaskInterface getInterface() {
        return tTask.getInterface();
    }
    
//    /**
//     * Returns description of the Task.
//     * @param lang
//     * @param contentType
//     * @param task
//     * @return
//     */
//    public String getDescription(String lang, String contentType, Task task) {
//
//        Validate.notNull(lang);
//        Validate.notNull(contentType);
//        Validate.notNull(task);
//        
//        String descriptionTamplate = null;
//
//        List<TDescription> tDescriptions = this.tTask.getPresentationElements().getDescription();
//        for (TDescription x : tDescriptions) {
//            if (lang.equals(x.getLang()) && contentType.equals(x.getContentType())) {
//                descriptionTamplate = x.getContent().get(0).toString();
//                break;
//            }
//        }
//        
//        if (descriptionTamplate == null) {
//            return "error";
//        }
//
//        //retrieve presentation parameters
//        Map<String, Object> presentationParameters = task.getPresentationParameterValues();
//
//        return this.templateEngine.merge(descriptionTamplate, presentationParameters).trim();
//    }

//    /**
//     * Returns task's priority. 0 is the highest priority, larger numbers identify lower priorities.
//     * @param task  The task priority is evaluated for.
//     * @return      Priority or null if it is not specified.
//     */
//    public Integer getPriority(Task task) {
//        
//        Validate.notNull(task);
//        
//        if (this.tTask.getPriority() != null) {
//            return Integer.parseInt(task.evaluateExpression(tTask.getPriority()).toString());
//        }
//        
//        return null;
//    }
    
//    /**
//     * Returns values of Task presentation parameters.
//     * @param task      The task presentation parameters values are evaluated for.
//     * @return          Map from parameter name to its value.
//     */
//    public Map<String, Object> getTaskPresentationParameters(Task task) {
//
//        Validate.notNull(task);
//
//        Map<String, Object> result = new HashMap<String, Object>();
//
//        List<TPresentationParameter> presentationParameters = this.tTask.getPresentationElements().getPresentationParameters().getPresentationParameter();
//
//        for(TPresentationParameter presentationParameter : presentationParameters) {
//
//            //TODO do not instantiate each time
//            QName returnType = new XmlUtils().getReturnType(presentationParameter.getType());
//            String parameterName = presentationParameter.getName().trim();
//            String parameterXPath = presentationParameter.getContent().get(0).toString().trim();
//            
//            Validate.notNull(returnType);
//            Validate.notNull(parameterName);
//            Validate.notNull(parameterXPath);
//
//            TExpression e = new TExpression();
//            e.getContent().add(parameterXPath);
//            Object o = task.evaluateExpression(e);
//            result.put(parameterName, o);
//        }
//
//        return result;
//    }

//    /**
//     * Evaluates assignees of generic human role.
//     * @param humanRoleName The generic human role.
//     * @param task          The task instance we evaluate assignees for.
//     * @return list of task assignees or empty list, when no assignments were made to this task.
//     */
//    public Set<Assignee> evaluateHumanRoleAssignees(GenericHumanRole humanRoleName, Task task) {
//        
//        Validate.notNull(humanRoleName);
//        Validate.notNull(task);
//        
//        Set<Assignee> evaluatedAssigneeList = new HashSet<Assignee>();
//        
//        //look for logical people group
//        List<JAXBElement<TGenericHumanRole>> ghrList = this.tTask.getPeopleAssignments().getGenericHumanRole();
//        for (int i=0; i < ghrList.size(); i++) {
//            
//            JAXBElement<TGenericHumanRole> ghr = ghrList.get(i);
//            
//            if (ghr.getName().getLocalPart().equals(humanRoleName.toString())) {
//
//                if (ghr.getValue().getFrom() != null && ghr.getValue().getFrom().getLogicalPeopleGroup() != null) { 
//                    String logicalPeopleGroupName = ghr.getValue().getFrom().getLogicalPeopleGroup().toString();
//                    List<Assignee> peopleQueryResult = this.peopleQuery.evaluate(logicalPeopleGroupName, null);
//                    evaluatedAssigneeList.addAll(peopleQueryResult);
//                }
//            }
//        }
//
//        //look for literal
//        for (JAXBElement<TGenericHumanRole> ghr : this.tTask.getPeopleAssignments().getGenericHumanRole()) {
//
//            if (humanRoleName.toString().equals(ghr.getName().getLocalPart())) {
//                
//                //get extension element by localPart name
//                assert ghr.getValue().getFrom() != null;
//                log.debug(ghr.getValue().getFrom().getContent());
//                Element e = (Element) new XmlUtils().getElementByLocalPart(ghr.getValue().getFrom().getContent(), "literal");
//                if (e != null) {
//                    
//                    Node organizationalEntityNode = e.getFirstChild();
//                    
//                    try {
//                        
//                        JAXBContext jaxbContext = JAXBContext.newInstance("org.apache.hise.lang.xsd.htd");
//                        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
//                        //InputStream is = organizationalEntity
//                        JAXBElement<TOrganizationalEntity> organizationalEntity = (JAXBElement<TOrganizationalEntity>) unmarshaller.unmarshal(organizationalEntityNode);
//                        
//                        TGrouplist groupList =  organizationalEntity.getValue().getGroups();
//                        if (groupList != null) {
//                            for (String group : groupList.getGroup()) {
//                                evaluatedAssigneeList.add(new Group(group));
//                            }
//                        }
//                        
//                        TUserlist userList =  organizationalEntity.getValue().getUsers();
//                        if (userList != null) {
//                            for (String user : userList.getUser()) {
//                                evaluatedAssigneeList.add(new Person(user));
//                            }
//                        }
//                        
//                    } catch (JAXBException e2) {
//                        log.error(e2);
//                        throw  new RuntimeException("Error evaluating human role for task: " + this.tTask.getName());
//                    }
//                }
//            }
//        }
//
//        return evaluatedAssigneeList;
//    }

//    /**
//     * TODO test
//     * Returns task's presentation name in a required language.
//     * @param lang subject language according ISO, e.g. en-US, pl, de-DE
//     * @return name
//     */
//    public String getName(String lang) {
//
//        Validate.notNull(lang);
//        
//        List<TText> tTexts = this.tTask.getPresentationElements().getName();
//        for (TText x : tTexts) {
//            if (lang.equals(x.getLang())) {
//                return x.getContent().get(0).toString();
//            }
//        }
//        
//        return "error";
//    }
//
//    /**
//     * Returns a task subject in a required language.
//     * @param lang subject language according ISO, e.g. en-US, pl, de-DE
//     * @param task The task subject value is evaluated for.
//     * @return subject
//     */
//    public String getSubject(String lang, Task task) {
//        
//        Validate.notNull(lang);
//        Validate.notNull(task);
//        
//        String subjectTemplate = null;
//
//        List<TText> tTexts = this.tTask.getPresentationElements().getSubject();
//        for (TText x : tTexts) {
//            if (lang.equals(x.getLang())) {
//                subjectTemplate = x.getContent().get(0).toString();
//                break;
//            }
//        }
//        
//        if (subjectTemplate == null) {
//            return "error";
//        }
//        
//        Map<String, Object> presentationParameterValues = task.getPresentationParameterValues();
//
//        return this.templateEngine.merge(subjectTemplate, presentationParameterValues).trim();
//    }

    public QName getTaskName() {
        return DOMUtils.uniqueQName(new QName(targetNamespace, this.tTask.getName()));
    }

    public TTask gettTask() {
        return tTask;
    }

    /**
     * Returns namespace URI for namespace registered in HumanInteractionsManager.
     * @param prefix     The xml namespace prefix.
     * @return namespace Namespace URI or null if it does not exist for a given prefix.
     */
    public String getNamespaceURI(String prefix) {
        return this.xmlNamespaces == null ? null : this.xmlNamespaces.get(prefix);
    }

}
