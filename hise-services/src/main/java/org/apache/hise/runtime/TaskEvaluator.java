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

package org.apache.hise.runtime;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import net.sf.saxon.value.DurationValue;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.dao.GenericHumanRole;
import org.apache.hise.dao.TaskOrgEntity;
import org.apache.hise.dao.TaskOrgEntity.OrgEntityType;
import org.apache.hise.lang.xsd.htd.TDeadline;
import org.apache.hise.lang.xsd.htd.TDeadlines;
import org.apache.hise.lang.xsd.htd.TEscalation;
import org.apache.hise.lang.xsd.htd.TExpression;
import org.apache.hise.lang.xsd.htd.TFrom;
import org.apache.hise.lang.xsd.htd.TGenericHumanRole;
import org.apache.hise.lang.xsd.htd.TPeopleAssignments;
import org.apache.hise.lang.xsd.htd.TPresentationParameter;
import org.apache.hise.lang.xsd.htd.TToPart;
import org.apache.hise.lang.xsd.htd.TToParts;
import org.apache.hise.utils.DOMUtils;
import org.apache.hise.utils.XQueryEvaluator;
import org.apache.hise.utils.XmlUtils;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

public class TaskEvaluator {
    
    private Log __log = LogFactory.getLog(TaskEvaluator.class);

    private Task task;
    
    public TaskEvaluator(Task task) {
        this.task = task;
    }

    public static class HtdFunctions {
        public static Node getInput(String part) {
            TaskEvaluator te = (TaskEvaluator) XQueryEvaluator.contextObjectTL.get();
            try {
                if(te.task.getTaskDto().getInput().get(part) != null) {
                    return DOMUtils.parse(te.task.getTaskDto().getInput().get(part).getMessage()).getDocumentElement();
                } else {
                    return null;
                }
            } catch (Exception e) {
                throw new RuntimeException("", e);
            }
        }
    }
    
    public XQueryEvaluator buildQueryEvaluator() {
        XQueryEvaluator evaluator = new XQueryEvaluator();
        evaluator.setContextObject(this);
        evaluator.declareNamespace("htd", "http://www.example.org/WS-HT");
        evaluator.declareJavaClass("http://www.example.org/WS-HT", HtdFunctions.class);
        evaluator.bindVariable(QName.valueOf("taskId"), task.getTaskDto().getId());
        evaluator.bindVariable(QName.valueOf("currentEventDateTime"), task.getCurrentEventDateTime());
        return evaluator;
    }
    
    public Integer evaluatePriority() {
        return Integer.parseInt("" + evaluateExpression(task.getTaskDefinition().getPriority()));
    }

    private List evaluateExpression(TExpression expr) {
        return buildQueryEvaluator().evaluateExpression(XmlUtils.getStringContent(expr.getContent()), null);
    }
    
    public Date evaluateDeadline(TDeadline deadline) {
        return (Date) buildQueryEvaluator().evaluateExpression("$currentEventDateTime + xs:dayTimeDuration(" + XmlUtils.getStringContent(deadline.getFor().getContent()) + ")", null).get(0);
    }
    
    public Set<TaskOrgEntity> evaluatePeopleAssignments() {
        Set<TaskOrgEntity> result = new HashSet<TaskOrgEntity>();
        TPeopleAssignments p = task.getTaskDefinition().getPeopleAssignments();

        for (JAXBElement<TGenericHumanRole> r : p.getGenericHumanRole()) {
            GenericHumanRole assignmentRole = GenericHumanRole.valueOf(r.getName().getLocalPart().toUpperCase());
            Set<TaskOrgEntity> result2 = evaluateGenericHumanRole(r.getValue(), assignmentRole);
            result.addAll(result2);
        }
        if (__log.isDebugEnabled()) {
            __log.debug("evaluated people assignments " + task.getTaskDefinition().getTaskName() + " " + result);
        }
        return result;
    }

    public Set<TaskOrgEntity> evaluateGenericHumanRole(TGenericHumanRole role, GenericHumanRole assignmentRole) {
        Set<TaskOrgEntity> result = new HashSet<TaskOrgEntity>();

        TFrom f = role.getFrom();
        if (f.getLogicalPeopleGroup() != null) {
            throw new NotImplementedException();
        } else {
            Element e = DOMUtils.findElement(QName.valueOf("{http://www.example.org/WS-HT}literal"), f.getContent());
            if (e != null) {
                for (String user : (List<String>) buildQueryEvaluator().evaluateExpression("declare namespace htd='http://www.example.org/WS-HT'; for $i in htd:literal/htd:organizationalEntity/htd:users/htd:user return string($i)", e)) {
                    TaskOrgEntity x = new TaskOrgEntity();
                    x.setGenericHumanRole(assignmentRole);
                    x.setName(user);
                    x.setType(OrgEntityType.USER);
                    x.setTask(task.getTaskDto());
                    result.add(x);
                }
                for (String group : (List<String>) buildQueryEvaluator().evaluateExpression("declare namespace htd='http://www.example.org/WS-HT'; for $i in htd:literal/htd:organizationalEntity/htd:groups/htd:group return string($i)", e)) {
                    TaskOrgEntity x = new TaskOrgEntity();
                    x.setGenericHumanRole(assignmentRole);
                    x.setName(group);
                    x.setType(OrgEntityType.GROUP);
                    x.setTask(task.getTaskDto());
                    result.add(x);
                }
            }
        }
        return result;
    }

    public Node createEprFromHeader(Node header) {
        return (Node) buildQueryEvaluator().evaluateExpression("<wsa:EndpointReference xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">{ */wsa:ReplyTo/* }</wsa:EndpointReference>", header).get(0);
    }
    
    // private Set<TaskOrgEntity> evaluatePeopleGroup(String groupName) {
    // HashSet<TaskOrgEntity> s = new HashSet<TaskOrgEntity>();
    // TaskOrgEntity o = new TaskOrgEntity();
    // o.setName("user1");
    // o.setType(OrgEntityType.USER);
    // s.add(o);
    // return s;
    // }
    
    public Node evaluateOutcome(boolean outcome) {
        XQueryEvaluator evaluator = buildQueryEvaluator();
        evaluator.bindVariable(QName.valueOf("outcome"), outcome);
        return (Node) evaluator.evaluateExpression(task.getTaskDefinition().getOutcomeExpression(), null).get(0);
    }
    
    public Node evaluateApproveResponseHeader() {
        XQueryEvaluator evaluator = buildQueryEvaluator();
        return (Node) evaluator.evaluateExpression("<htd:taskId xmlns:htd=\"http://www.example.org/WS-HT\">{$taskId}</htd:taskId>", null).get(0);
    }

    
    public static String getEscalationKey(TEscalation e, boolean isCompletion) {
        return e.getName() + ";" + (isCompletion ? "COMPLETION" : "START");
    }
    
    public static class EscalationResult {
        public final TEscalation escalation;
        public final boolean isCompletion;
        public EscalationResult(TEscalation escalation, boolean isCompletion) {
            super();
            this.escalation = escalation;
            this.isCompletion = isCompletion;
        }
    }
    
    public EscalationResult findEscalation(String name) {
        EscalationResult r = null;
        TDeadlines d = task.getTaskDefinition().getDeadlines();
        for (TDeadline u : d.getStartDeadline()) {
            for (TEscalation e : u.getEscalation()) {
                if (getEscalationKey(e, false).equals(name)) {
                    return new EscalationResult(e, false);
                }
            }
        }

        for (TDeadline u : d.getCompletionDeadline()) {
            for (TEscalation e : u.getEscalation()) {
                if (getEscalationKey(e, true).equals(name)) {
                    return new EscalationResult(e, true);
                }
            }
        }
        
        return null;
    }
    
    public Map<String, Node> evaluateToParts(TToParts toParts) {
        Map<String, Node> result = new HashMap<String, Node>();
        if (toParts != null) {
            XQueryEvaluator e = buildQueryEvaluator();
            for (TToPart toPart : toParts.getToPart()) {
                result.put(toPart.getName(), (Node) e.evaluateExpression(XmlUtils.getStringContent(toPart.getContent()), null).get(0));
            }
        }
        return result;
    }
    
    public static Node defaultHeader() {
        return DOMUtils.parse("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<soapenv:Header xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"/>").getDocumentElement();
    }
    
    public String getPresentationName() {
    	try {
    		return XmlUtils.getStringContent(task.getTaskDefinition().getPresentationElements().getName().get(0).getContent());
    	} catch (Throwable t) {
    		return null;
    	}
    }
    
    public XQueryEvaluator buildPresentationEvaluator() {
        XQueryEvaluator evaluator = buildQueryEvaluator();
        for (TPresentationParameter p : task.getTaskDefinition().getPresentationParameters()) {
            XQueryEvaluator evaluator2 = buildQueryEvaluator();
            List eavaluationResult = evaluator2.evaluateExpression(XmlUtils.getStringContent(p.getContent()), null);
            Object v = null;
            if (!eavaluationResult.isEmpty()) {
                v = eavaluationResult.get(0);
            } else {
                v = "ERROR";
                __log.warn("Could not evaluate presentationParameter: " + p.getName());
            }
            __log.debug("evaluated presentationParameter: " + p.getName() + " = " + v);
        	evaluator.bindVariable(QName.valueOf(p.getName()), v);
        }
        return evaluator;
    }
    
    public static String getTemplateExpr(List<Object> expr) {
    	return "xs:string(<v>" + XmlUtils.getStringContent(expr) + "</v>)";
    }
    
    
    public String evalPresentationSubject() {
    	String subjectExpr;
    	try {
	    	subjectExpr = getTemplateExpr(task.getTaskDefinition().getPresentationElements().getSubject().get(0).getContent());
		} catch (Throwable t) {
			return null;
		}
		XQueryEvaluator e = buildPresentationEvaluator();
		return "" + e.evaluateExpression(subjectExpr, null).get(0);
    }
    
    public String evalPresentationDescription() {
    	String descExpr;
    	try {
	    	descExpr = getTemplateExpr(task.getTaskDefinition().getPresentationElements().getDescription().get(0).getContent());
		} catch (Throwable t) {
			return null;
		}
		XQueryEvaluator e = buildPresentationEvaluator();
		return "" + e.evaluateExpression(descExpr, null).get(0);
    }
}
