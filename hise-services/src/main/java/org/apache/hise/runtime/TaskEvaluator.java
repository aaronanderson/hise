package org.apache.hise.runtime;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.dom.NodeWrapper;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.dao.TaskOrgEntity;
import org.apache.hise.dao.GenericHumanRole;
import org.apache.hise.dao.TaskOrgEntity.OrgEntityType;
import org.apache.hise.lang.xsd.htd.ObjectFactory;
import org.apache.hise.lang.xsd.htd.TExpression;
import org.apache.hise.lang.xsd.htd.TFrom;
import org.apache.hise.lang.xsd.htd.TGenericHumanRole;
import org.apache.hise.lang.xsd.htd.TLiteral;
import org.apache.hise.lang.xsd.htd.TPeopleAssignments;
import org.apache.hise.utils.DOMUtils;
import org.apache.hise.utils.XQueryEvaluator;
import org.apache.hise.utils.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class TaskEvaluator {
    private static Log __log = LogFactory.getLog(TaskEvaluator.class);

    private Task task;
    
    private XQueryEvaluator evaluator = new XQueryEvaluator();

    public TaskEvaluator(Task task) {
        this.task = task;
    }

    public Integer evaluatePriority() {
        return Integer.parseInt("" + evaluateExpression(task.getTaskDefinition().gettTask().getPriority()));
    }

    public Object evaluateExpression(TExpression expr) {
        return evaluator.evaluateExpression(XmlUtils.getStringContent(expr.getContent()), null);
    }

    public Set<TaskOrgEntity> evaluatePeopleAssignments() {
        Set<TaskOrgEntity> result = new HashSet<TaskOrgEntity>();
        TPeopleAssignments p = task.getTaskDefinition().gettTask().getPeopleAssignments();

        for (JAXBElement<TGenericHumanRole> r : p.getGenericHumanRole()) {
            GenericHumanRole assignmentRole = GenericHumanRole.valueOf(r.getName().getLocalPart().toUpperCase());
            TGenericHumanRole role = r.getValue();
            TFrom f = role.getFrom();
            if (f.getLogicalPeopleGroup() != null) {
                // TODO
            } else {
                Element e = DOMUtils.findElement(QName.valueOf("{http://www.example.org/WS-HT}literal"), f.getContent());
                if (e != null) {
                    for (String user : (List<String>) evaluator.evaluateExpression("declare namespace htd='http://www.example.org/WS-HT'; for $i in htd:literal/htd:organizationalEntity/htd:users/htd:user return string($i)", e)) {
                        TaskOrgEntity x = new TaskOrgEntity();
                        x.setGenericHumanRole(assignmentRole);
                        x.setName(user);
                        x.setType(OrgEntityType.USER);
                        x.setTask(task.getTaskDto());
                        result.add(x);
                    }
                    for (String group : (List<String>) evaluator.evaluateExpression("declare namespace htd='http://www.example.org/WS-HT'; for $i in htd:literal/htd:organizationalEntity/htd:groups/htd:group return string($i)", e)) {
                        TaskOrgEntity x = new TaskOrgEntity();
                        x.setGenericHumanRole(assignmentRole);
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

    public Node createEprFromHeader(Node header) {
        return NodeOverNodeInfo.wrap((NodeInfo) evaluator.evaluateExpression("<wsa:EndpointReference xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">{ */wsa:ReplyTo/* }</wsa:EndpointReference>", header).get(0));
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
