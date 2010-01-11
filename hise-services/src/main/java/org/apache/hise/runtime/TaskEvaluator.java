package org.apache.hise.runtime;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.dom.NodeWrapper;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.dao.TaskOrgEntity;
import org.apache.hise.dao.TaskOrgEntity.AssignmentRole;
import org.apache.hise.dao.TaskOrgEntity.OrgEntityType;
import org.apache.hise.lang.xsd.htd.ObjectFactory;
import org.apache.hise.lang.xsd.htd.TExpression;
import org.apache.hise.lang.xsd.htd.TFrom;
import org.apache.hise.lang.xsd.htd.TGenericHumanRole;
import org.apache.hise.lang.xsd.htd.TLiteral;
import org.apache.hise.lang.xsd.htd.TPeopleAssignments;
import org.apache.hise.utils.DOMUtils;
import org.apache.hise.utils.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

public class TaskEvaluator {
    private static Log __log = LogFactory.getLog(TaskEvaluator.class);

    private Task task;

    public TaskEvaluator(Task task) {
        this.task = task;
    }

    public Integer evaluatePriority() {
        return Integer.parseInt("" + evaluateExpression(task.getTaskDefinition().gettTask().getPriority()));
    }

    public List evaluateExpression(String expr, org.w3c.dom.Node contextNode) {
        Configuration config = Configuration.makeConfiguration(null, null);
        StaticQueryContext sqc = new StaticQueryContext(config);
        DynamicQueryContext dqc = new DynamicQueryContext(config);
        try {
            XQueryExpression e = sqc.compileQuery(expr);
            if (contextNode != null) {
                if (!(contextNode instanceof Document || contextNode instanceof DocumentFragment) ) {
                    DocumentFragment frag = contextNode.getOwnerDocument().createDocumentFragment();
                    frag.appendChild(contextNode);
                    contextNode = frag;
                }
                dqc.setContextItem(new DocumentWrapper(contextNode, "", config));
            }

            List value = e.evaluate(dqc);
            __log.debug("result for expression " + expr + " " + value + " value class " + (value == null ? null : value.getClass()));
            return value;
        } catch (XPathException e) {
            __log.error("", e);
            throw new RuntimeException(e);
        }
    }

    public Object evaluateExpression(TExpression expr) {
        return evaluateExpression(XmlUtils.getStringContent(expr.getContent()), null);
    }

    public Set<TaskOrgEntity> evaluatePeopleAssignments() {
        Set<TaskOrgEntity> result = new HashSet<TaskOrgEntity>();
        TPeopleAssignments p = task.getTaskDefinition().gettTask().getPeopleAssignments();

        for (JAXBElement<TGenericHumanRole> r : p.getGenericHumanRole()) {
            AssignmentRole assignmentRole = TaskOrgEntity.AssignmentRole.valueOf(r.getName().getLocalPart().toUpperCase());
            TGenericHumanRole role = r.getValue();
            TFrom f = role.getFrom();
            if (f.getLogicalPeopleGroup() != null) {
                // TODO
            } else {
                Element e = DOMUtils.findElement(QName.valueOf("{http://www.example.org/WS-HT}literal"), f.getContent());
                if (e != null) {
                    for (String user : (List<String>) evaluateExpression("declare namespace htd='http://www.example.org/WS-HT'; for $i in htd:literal/htd:organizationalEntity/htd:users/htd:user return string($i)", e)) {
                        TaskOrgEntity x = new TaskOrgEntity();
                        x.setAssignmentRole(assignmentRole);
                        x.setName(user);
                        x.setType(OrgEntityType.USER);
                        x.setTask(task.getTaskDto());
                        result.add(x);
                    }
                    for (String group : (List<String>) evaluateExpression("declare namespace htd='http://www.example.org/WS-HT'; for $i in htd:literal/htd:organizationalEntity/htd:groups/htd:group return string($i)", e)) {
                        TaskOrgEntity x = new TaskOrgEntity();
                        x.setAssignmentRole(assignmentRole);
                        x.setName(group);
                        x.setType(OrgEntityType.GROUP);
                        x.setTask(task.getTaskDto());
                        result.add(x);
                    }
                }
            }
        }
        if (__log.isDebugEnabled()) {
            __log.debug("evaluated people assignments " + task.getTaskDefinition().getTaskName() + " " + result);
        }
        return result;
    }

    // private Set<TaskOrgEntity> evaluatePeopleGroup(String groupName) {
    // HashSet<TaskOrgEntity> s = new HashSet<TaskOrgEntity>();
    // TaskOrgEntity o = new TaskOrgEntity();
    // o.setName("user1");
    // o.setType(OrgEntityType.USER);
    // s.add(o);
    // return s;
    // }

}
