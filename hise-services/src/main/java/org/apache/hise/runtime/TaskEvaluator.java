package org.apache.hise.runtime;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.saxon.Configuration;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.dao.TaskOrgEntity;
import org.apache.hise.dao.TaskOrgEntity.OrgEntityType;
import org.apache.hise.lang.xsd.htd.TExpression;
import org.apache.hise.utils.XmlUtils;

public class TaskEvaluator {
    private static Log __log = LogFactory.getLog(TaskEvaluator.class);
    
    private Task task;
    
    public TaskEvaluator(Task task) {
        this.task = task;
    }
    
    public Integer evaluatePriority() {
        return Integer.parseInt("" + evaluateExpression(task.getTaskDefinition().gettTask().getPriority()));
    }

    public Object evaluateExpression(String expr) {
        Configuration config = Configuration.makeConfiguration(null, null);
        StaticQueryContext sqc = new StaticQueryContext(config);
        DynamicQueryContext dqc = new DynamicQueryContext(config);
        try {
            XQueryExpression e = sqc.compileQuery(expr);
            Object value = e.evaluateSingle(dqc);
            __log.debug("result for expression " + expr + " " + value + " value class " + value == null ? null : value.getClass());
            return value;
        } catch (XPathException e) {
            __log.error("", e);
            throw new RuntimeException(e);
        }
    }
    
    public Object evaluateExpression(TExpression expr) {
        return evaluateExpression(XmlUtils.getStringContent(expr.getContent()));
    }

    public Set<TaskOrgEntity> evaluatePotentialOwners() {
        return evaluatePeopleGroup("potentialOwners");
    }
    
    private Set<TaskOrgEntity> evaluatePeopleGroup(String groupName) {
        HashSet<TaskOrgEntity> s = new HashSet<TaskOrgEntity>();
        TaskOrgEntity o = new TaskOrgEntity();
        o.setName("user1");
        o.setType(OrgEntityType.USER);
        s.add(o);
        return s;
    }

}
