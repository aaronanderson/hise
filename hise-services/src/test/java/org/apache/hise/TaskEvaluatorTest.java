package org.apache.hise;

import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.Assert;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.om.NodeInfo;

import org.apache.hise.lang.xsd.htd.TDeadline;
import org.apache.hise.lang.xsd.htd.TDurationExpr;
import org.apache.hise.runtime.Task;
import org.apache.hise.runtime.TaskEvaluator;
import org.apache.hise.utils.DOMUtils;
import org.apache.hise.utils.XQueryEvaluator;
import org.junit.Test;
import org.w3c.dom.Node;

public class TaskEvaluatorTest {
    
    private TaskEvaluator buildTaskEvaluator() {
        Task t = new MockTask();
        t.setCurrentEventDateTime(new Date(1234L));
        org.apache.hise.dao.Task t2 = new org.apache.hise.dao.Task();
        t2.setId(1234L);
        t.setTaskDto(t2);
        
        return new TaskEvaluator(t);
    }
    
    @Test
    public void testEval() throws Exception {
        XQueryEvaluator e = new XQueryEvaluator();
        List r = e.evaluateExpression("string(*/*/text())", DOMUtils.parse("<a><b/><c>ddx</c></a>"));
        Assert.assertTrue(r.toString().equals("[ddx]"));
    }

    @Test
    public void testEval2() throws Exception {
        XQueryEvaluator e = new XQueryEvaluator();
        e.bindVariable(QName.valueOf("abc"), null);
        Object r = e.evaluateExpression("declare namespace htd='http://www.example.org/WS-HT'; for $i in htd:literal/htd:organizationalEntity/htd:users/htd:user return string($i)", DOMUtils.parse(getClass().getResourceAsStream("/taskEvaluator.xml")));
        Assert.assertTrue(r.toString().equals("[user1, user2]"));
    }

    @Test
    public void testEval3() throws Exception {
        XQueryEvaluator e = new XQueryEvaluator();
        Object r = e.evaluateExpression("declare namespace htd='http://www.example.org/WS-HT'; for $i in htd:literal/htd:organizationalEntity/htd:users/htd:user return string($i)", DOMUtils.parse(getClass().getResourceAsStream("/taskEvaluator.xml")).getFirstChild());
        Assert.assertTrue(r.toString().equals("[user1, user2]"));
    }
    
    @Test
    public void testEvalOutcome() throws Exception {
        XQueryEvaluator e = new XQueryEvaluator();
        e.bindVariable(QName.valueOf("outcome"), true);
        Assert.assertEquals(true, e.evaluateExpression("$outcome", null).get(0));
    }
    
    @Test
    public void testEvaluateApproveResponseHeader() throws Exception {
        TaskEvaluator te = buildTaskEvaluator();
        Node n = te.evaluateApproveResponseHeader();
        Assert.assertTrue(DOMUtils.domToString(n).contains(">1234<"));
    }
    
    @Test
    public void testDeadline() throws Exception {
        TaskEvaluator e = buildTaskEvaluator();

        TDeadline deadline = new TDeadline();
        TDurationExpr d = new TDurationExpr();
        deadline.setFor(d);
        d.getContent().add(new String("'PT5S'"));
        Date r = e.evaluateDeadline(deadline);
        Assert.assertEquals(6234L, r.getTime());
    }


}
