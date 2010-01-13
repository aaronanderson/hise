package org.apache.hise.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.functions.JavaExtensionLibrary;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

public class XQueryEvaluator {
    private static Log __log = LogFactory.getLog(XQueryEvaluator.class);

    public static ThreadLocal<Object> contextObjectTL = new ThreadLocal<Object>() ;
    
    private Map<QName, Object> vars = new HashMap<QName, Object>();
    private Configuration config = Configuration.makeConfiguration(null, null);
    private JavaExtensionLibrary jel = new JavaExtensionLibrary(config);
    
    private Object contextObject;
    
    public void bindVariable(QName var, Object value) {
        vars.put(var, value);
    }
    
    public void declareJavaClass(String uri, Class clazz) {
        jel.declareJavaClass(uri, clazz);
    }

    public void setContextObject(Object contextObject) {
        this.contextObject = contextObject;
    }

    public static ValueRepresentation convertJavaToSaxon(Object obj) {
        try {
            return JPConverter.allocate(obj.getClass(), null).convert(obj, null);
        } catch (XPathException e) {
            throw new RuntimeException("", e);
        }
    }
    
    public List evaluateExpression(String expr, org.w3c.dom.Node contextNode) {
        try {
            contextObjectTL.set(contextObject);
            {
                FunctionLibraryList fll = new FunctionLibraryList();
                fll.addFunctionLibrary(jel);
                config.setExtensionBinder("java", fll);
            }

            StaticQueryContext sqc = new StaticQueryContext(config);
            for (QName var : vars.keySet()) {
                sqc.declareGlobalVariable(StructuredQName.fromClarkName(var.toString()), SequenceType.SINGLE_ITEM, convertJavaToSaxon(vars.get(var)) , false);
            }
            DynamicQueryContext dqc = new DynamicQueryContext(config);
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
            List value2 = new ArrayList();
            for (Object o : value) {
                Object o2 = o;
                if (o2 instanceof NodeInfo) {
                    try {
                        Node o3 = DOMUtils.parse(DOMUtils.domToString(NodeOverNodeInfo.wrap((NodeInfo) o2))).getDocumentElement();
                        o2 = o3;
                    } catch (Exception e1) {
                        throw new RuntimeException("Error converting result", e1);
                    }
                }
                value2.add(o2);
            }
            __log.debug("result for expression " + expr + " " + value2 + " value class " + (value2 == null ? null : value2.getClass()));
            return value2;
        } catch (XPathException e) {
            __log.error("", e);
            throw new RuntimeException(e);
        } finally {
            contextObjectTL.set(null);
        }
    }
}
