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

package org.apache.hise.engine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.om.NodeInfo;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.dao.Message;
import org.apache.hise.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class TaskXmlUtils {
    
    private final Log __log = LogFactory.getLog(TaskXmlUtils.class);

    private XPathFactory xPathFactory = null;
    
    private NamespaceContext namespaceContext;
    
    private Map<String, Message> input;
    private Map<String, Message> output;

    public TaskXmlUtils(NamespaceContext namespaceContext, Map<String, Message> input, Map<String, Message> output) {
        super();
        this.xPathFactory = DOMUtils.getXPathFactory();
        this.namespaceContext = namespaceContext;
        this.input = input;
        this.output = output;
    }
    
    /**
     * Creates {@link XPath} aware of request namespaces.
     */
    synchronized XPath createXPathInstance() {
        
        XPath xpath = this.xPathFactory.newXPath();

        xpath.setNamespaceContext(this.namespaceContext);
        xpath.setXPathFunctionResolver(new XPathFunctionResolver() {

            public XPathFunction resolveFunction(QName functionName, int arity) {
    
                if (functionName == null) {
                    throw new NullPointerException("The function name cannot be null.");
                }
    
                if (functionName.equals(new QName("http://www.example.org/WS-HT", "getInput", "htd"))) {
    
                    return new GetXPathFunction(input);
                }
                
                if (functionName.equals(new QName("http://www.example.org/WS-HT", "getOutput", "htd"))) {
    
                    return new GetXPathFunction(output);
                } 
                    
                return null;
            }
        });

        return xpath;
    }
    
    /**
     * Evaluates XPath expression in context of the Task. Expression can contain 
     * XPath Extension functions as defined by WS-HumanTask v1. Following
     * XPath functions are implemented:
     * <ul>
     * <li> {@link GetInputXPathFunction} </li>
     * <li> {@link GetOutputXPathFunction} </li>
     * </ul>
     * @param xPathString The XPath 1.0 expression.
     * @param returnType The desired return type. See {@link XPathConstants}.
     * @return The result of evaluating the <code>XPath</code> function as an <code>Object</code>.
     */
    public Object evaluateXPath(String xPathString, QName returnType) {
        
        Validate.notNull(xPathString);
        Validate.notNull(returnType);

        Object o = null;

        XPath xpath = createXPathInstance();

        try {

            //TODO create empty document only once
            DocumentBuilder builder = DOMUtils.getDocumentBuilderFactory().newDocumentBuilder();
            Document emptyDocument;
            emptyDocument = builder.parse(new ByteArrayInputStream("<empty/>".getBytes()));
            XPathExpression expr = xpath.compile(xPathString);
            o = expr.evaluate(emptyDocument, returnType);
            __log.debug("evaluated " + o);
            if (o instanceof NodeInfo) {
                NodeInfo o2 = (NodeInfo) o;
                Node o3 = NodeOverNodeInfo.wrap(o2);
                __log.debug("returned " + DOMUtils.domToString(o3));
                return o3;
            } else {
                return o;
            }
        } catch (Exception e) {
            __log.error("Error evaluating XPath: " + xPathString, e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Implements getInput {@link XPathFunction} - get the data for the part of the task's input message.
     * @author Witek Wołejszo
     */
    private static class GetXPathFunction implements XPathFunction {
        
        private final Log log = LogFactory.getLog(GetXPathFunction.class);
        private Map<String, Message> data;

        
        public GetXPathFunction(Map<String, Message> data) {
            this.data = data;
        }
        
        /**
         * <p>Evaluate the function with the specified arguments.</p>
         * @see XPathFunction#evaluate(List)
         * @param args The arguments, <code>null</code> is a valid value.
         * @return The result of evaluating the <code>XPath</code> function as an <code>Object</code>.
         * @throws XPathFunctionException If <code>args</code> cannot be evaluated with this <code>XPath</code> function.
         */
        public Object evaluate(List args) throws XPathFunctionException {

            String partName = (String) args.get(0);

            Message message = data.get(partName);
            Document document = null;
            
            if (message == null) {
                throw new XPathFunctionException("Task's input does not contain partName: " + args.get(0));
            }

            try {
                
                document = message.getDomDocument();
                
            } catch (ParserConfigurationException e) {

                throw new XPathFunctionException(e);
            } catch (SAXException e) {
                
                throw new XPathFunctionException(e);
            } catch (IOException e) {
                
                throw new XPathFunctionException(e);
            }
            
            if (document == null) return null;
            NodeList l = document.getElementsByTagName(partName);
            Validate.isTrue(l.getLength() == 1, "Exactly one part must exist.");
            return l.item(0);
        }

    }
}
