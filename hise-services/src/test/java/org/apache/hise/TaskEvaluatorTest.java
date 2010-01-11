package org.apache.hise;

import java.util.List;

import junit.framework.Assert;

import org.apache.hise.runtime.TaskEvaluator;
import org.apache.hise.utils.DOMUtils;
import org.junit.Test;

public class TaskEvaluatorTest {
    @Test
    public void testEval() throws Exception {
        TaskEvaluator e = new TaskEvaluator(null);
        List r = e.evaluateExpression("string(*/*/text())", DOMUtils.parse("<a><b/><c>ddx</c></a>"));
        Assert.assertTrue(r.toString().equals("[ddx]"));
    }

    @Test
    public void testEval2() throws Exception {
        TaskEvaluator e = new TaskEvaluator(null);
        Object r = e.evaluateExpression("declare namespace htd='http://www.example.org/WS-HT'; for $i in htd:literal/htd:organizationalEntity/htd:users/htd:user return string($i)", DOMUtils.parse(getClass().getResourceAsStream("/taskEvaluator.xml")));
        Assert.assertTrue(r.toString().equals("[user1, user2]"));
    }

    @Test
    public void testEval3() throws Exception {
        TaskEvaluator e = new TaskEvaluator(null);
        Object r = e.evaluateExpression("declare namespace htd='http://www.example.org/WS-HT'; for $i in htd:literal/htd:organizationalEntity/htd:users/htd:user return string($i)", DOMUtils.parse(getClass().getResourceAsStream("/taskEvaluator.xml")).getFirstChild());
        Assert.assertTrue(r.toString().equals("[user1, user2]"));
    }
}
