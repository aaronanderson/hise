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

package org.apache.hise.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathFactory;

import net.sf.saxon.Configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.dom.DOMOutputImpl;
import org.apache.xerces.impl.Constants;
import org.apache.xml.serialize.DOMSerializerImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.TypeInfo;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DOMUtils {
    
    public static QName uniqueQName(QName q) {
        String s = q.getNamespaceURI();
        while (s.endsWith("/")) { s = s.substring(0, s.length() - 1); }
        return new QName(s, q.getLocalPart());
    }

    public static Element getFirstElement(Node node) {
        NodeList l = node.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i) instanceof Element) {
                return (Element) l.item(i);
            }
        }
        return null;
    }
    
    public static Element findElement(QName elementName, List<Object> content) {
        for (Object o : content) {
            if (o instanceof Element) {
                Element u = (Element) o;
                QName n = new QName(u.getNamespaceURI(), u.getLocalName());
                if (n.equals(elementName)) {
                    return u;
                }
            }
        }
        return null;
    }
    
    /**
     * Convert a DOM node to a stringified XML representation.
     */
    static public String domToString(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Cannot stringify null Node!");
        }

        String value = null;
        short nodeType = node.getNodeType();
        if (nodeType == Node.ELEMENT_NODE || nodeType == Node.DOCUMENT_NODE) {
            // serializer doesn't handle Node type well, only Element
            DOMSerializerImpl ser = new DOMSerializerImpl();
            ser.setParameter(Constants.DOM_NAMESPACES, Boolean.TRUE);
            ser.setParameter(Constants.DOM_WELLFORMED, Boolean.FALSE );
            ser.setParameter(Constants.DOM_VALIDATE, Boolean.FALSE);

            // create a proper XML encoding header based on the input document;
            // default to UTF-8 if the parent document's encoding is not accessible
            String usedEncoding = "UTF-8";
            Document parent = node.getOwnerDocument();
            if (parent != null) {
                String parentEncoding = parent.getXmlEncoding();
                if (parentEncoding != null) {
                    usedEncoding = parentEncoding;
                }
            }

            // the receiver of the DOM
            DOMOutputImpl out = new DOMOutputImpl();
            out.setEncoding(usedEncoding);

            // we write into a String
            StringWriter writer = new StringWriter(4096);
            out.setCharacterStream(writer);

            // out, ye characters!
            ser.write(node, out);
            writer.flush();

            // finally get the String
            value = writer.toString();
        } else {
            value = node.getNodeValue();
        }
        return value;
    }

    public static DocumentBuilderFactory getDocumentBuilderFactory() {
        return new org.apache.xerces.jaxp.DocumentBuilderFactoryImpl();
//        return new net.sf.saxon.dom.DocumentBuilderFactoryImpl();
    }
    
    public static XPathFactory getXPathFactory() {
        return new net.sf.saxon.xpath.XPathFactoryImpl();
    }
    
    public static Document parse(InputStream in) throws Exception {
        DocumentBuilderFactory f = DOMUtils.getDocumentBuilderFactory();
        f.setNamespaceAware(true);
        DocumentBuilder b = f.newDocumentBuilder();
        Document d = b.parse(in);
        return d;
    }

    public static Document parse(String in) throws Exception {
        return parse(new ByteArrayInputStream(in.getBytes()));
    }
}
