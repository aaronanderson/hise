package org.apache.hise.utils;

import java.util.List;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

public class XQueryEvaluator {
    private static Log __log = LogFactory.getLog(XQueryEvaluator.class);
    
    public List evaluateExpression(String expr, org.w3c.dom.Node contextNode) {
        Configuration config = Configuration.makeConfiguration(null, null);
        StaticQueryContext sqc = new StaticQueryContext(config);
        DynamicQueryContext dqc = new DynamicQueryContext(config);
        try {
            XQueryExpression e = sqc.compileQuery(expr);
            if (contextNode != null) {
                if (!(contextNode instanceof Document || contextNode instanceof DocumentFragment) ) {
                    try {
                        contextNode = DOMUtils.parse(DOMUtils.domToString(contextNode));
                    } catch (Exception e1) {
                        throw new RuntimeException("", e1);
                    }
//                    DocumentFragment frag = contextNode.getOwnerDocument().createDocumentFragment();
//                    frag.appendChild(contextNode);
//                    contextNode = frag;
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
}
