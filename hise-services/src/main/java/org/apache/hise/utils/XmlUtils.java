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

import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Element;


/**
 * XML Functions and constants.
 * @author Witek Wołejszo
 */
public class XmlUtils {

//    public static final QName SCHEMA_STRING = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "string");
//    public static final QName SCHEMA_DOUBLE = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "double");
//    public static final QName SCHEMA_BOOLEAN = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "boolean");
//
//    /**
//     * Calculates XPath return type based on XMLSchema type.
//     * @param type Schema type: string, boolean or double.
//     * @return The return type. See {@link XPathConstants}.
//     */
//    public QName getReturnType(QName type) {
//        
//        if (type.equals(SCHEMA_STRING)) {
//            
//            return XPathConstants.STRING;
//            
//        } if (type.equals(SCHEMA_DOUBLE)) {
//        
//            return XPathConstants.NUMBER;
//            
//        } if (type.equals(SCHEMA_BOOLEAN)) {
//        
//            return XPathConstants.BOOLEAN;
//        }
//        
//        throw new RuntimeException("Cannot map: " + type + " to XPath result type.");
//    }
    
    public Object getElementByLocalPart(List<Object> any, String localPart) {
        for (Object o : any) {
            if (o instanceof Element) {
                Element e = (Element) o;
                if (localPart.equals(e.getLocalName())) {
                    return e;
                }
            }
        }
        return null;
    }
    
    public static String getStringContent(List<Object> content) {
        StringBuilder b = new StringBuilder();
        for (Object o : content) {
            if (o instanceof String) {
                b.append(o);
            }
        }
        return b.toString();
    }
    
//  //look for human role
//  //List<Object> x = this.tTask.getPeopleAssignments().getGenericHumanRole().get(0).getValue().getAny();
//  for (JAXBElement<TGenericHumanRole> ghr : this.tTask.getPeopleAssignments().getGenericHumanRole()) {
//      if (humanRoleName.toString().equals(ghr.getName().getLocalPart())) {
//          
//          List<Object> any = ghr.getValue().getAny();
//          for (Object o : any) {
//              Element e = (Element) o;
//              if ("literal".equals(e.getElementName().getLocalPart())) {
//                  //e.
//              }
//          }
//          
//      }
//  }
}
