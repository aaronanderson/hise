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

package org.apache.hise.runtime;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.utils.DOMUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Message part related to Task. It can be either part of input message or part of output message.
 * 
 * @author Witek Wołejszo
 */
@Entity
@Table(name = "MESSAGE")
public class Message extends Base {
    
    @Transient
    private final Log log = LogFactory.getLog(Message.class);
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "mssg_seq")
    @SequenceGenerator(name = "mssg_seq", sequenceName = "mssg_seq")
    private Long id;
    
    private String partName;

    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    @Lob
    private String message;
    
    @Transient
    private Document messageDocument;

    /**
     * Constructs Message.
     */
    public Message() {
        super();
    }
    
    /**
     * Constructs Message.
     * @param message
     */
    public Message(String message) {
        
        super();
        
        Validate.notNull(message);
        
        this.message = message;
        this.partName = this.getRootNodeName();
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public String getMessage() {
        return this.message;
    }

    public String getPartName() {
        return this.partName;
    }
    
    //operations
    
    /**
     * Returns {@link InputStream} with message contents using platform encoding. 
     * @return
     */
    private InputStream getMessageInputStream() {
        return new ByteArrayInputStream(this.message.getBytes());
    }
    
    /**
     * Returns DOM Document with parsed message part.
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public synchronized Document getDomDocument() throws ParserConfigurationException, SAXException, IOException {
        
        if (this.messageDocument == null) {
            DocumentBuilderFactory factory = DOMUtils.getDocumentBuilderFactory();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            this.messageDocument = builder.parse(this.getMessageInputStream());
        }
        
        return this.messageDocument;
    }
    
    /**
     * Returns root element name.
     * @return the root element name
     */
    public String getRootNodeName() {
        
        try {
            
            return this.getDomDocument().getDocumentElement().getNodeName();
            
        } catch (ParserConfigurationException e) {
            
            log.error(e);
            throw new RuntimeException("error gettung messages root element name", e);
            
        } catch (SAXException e) {
            
            log.error("error getting message's root element name", e);
            throw new RuntimeException("error getting message's root element name", e);
            
        } catch (IOException e) {
            
            log.error("error getting message's root element name", e);
            throw new RuntimeException("error getting message's root element name", e);
        }
    }

    /***************************************************************
     * Infrastructure methods.                                     *
     ***************************************************************/

    /**
     * Returns the message hashcode.
     * @return message hash code
     */
    @Override
    public int hashCode() {
        int result = ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /**
     * Checks whether the message is equal to another object.
     * @param obj object to compare
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Message == false) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        Message m = (Message) obj;
        return new EqualsBuilder().append(id, m.id).isEquals();
    }

}
