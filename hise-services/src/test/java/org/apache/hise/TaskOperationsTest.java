package org.apache.hise;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.validation.SchemaFactory;

import junit.framework.Assert;

import org.apache.hise.api.HISEUserDetails;
import org.apache.hise.engine.HISEEngineImpl;
import org.apache.hise.engine.jaxws.TaskOperationsImpl;
import org.apache.hise.lang.xsd.htda.TTask;
import org.apache.hise.lang.xsd.htdt.SuspendUntil;
import org.apache.hise.utils.XQueryEvaluator;
import org.junit.Test;
import org.junit.Ignore;

public class TaskOperationsTest {

    @Ignore
    public void testGetMyTasks() throws Exception {
        
        TaskOperationsImpl ti = new MockTaskOperationsImpl();
        
        HISEEngineImpl he = new HISEEngineImpl();
        he.setHiseUserDetails(new HISEUserDetails() {
            public String getUserPassword(String user) {
                return null;
            }
            
            public Collection<String> getUserGroups(String user) {
                return Collections.singleton("group1");
            }
        });
        MockHiseDao hd = new MockHiseDao();
        he.setHiseDao(hd);
        ti.setHiseEngine(he);
        
        List<TTask> r = ti.getMyTasks("ALL", "ACTUALOWNER", "", Collections.EMPTY_LIST, "", "", 100);
        System.out.println(r.toString());
        
        JAXBContext c = JAXBContext.newInstance("org.apache.hise.lang.xsd.htda");
        
        
        Marshaller m = c.createMarshaller();
        m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
        m.marshal(new JAXBElement(QName.valueOf("{http://www.example.org/WS-HT/api/xsd}taskAbstract"), TTask.class, r.get(0)), System.out);
    }
    
    @Test
    public void testSuspendUntil() throws Exception {
        JAXBContext c = JAXBContext.newInstance("org.apache.hise.lang.xsd.htdt");
        Unmarshaller m = c.createUnmarshaller();
        m.setSchema(SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema").newSchema(getClass().getResource("/ws-humantask-api-wsdl.xsd")));
        SuspendUntil e = (SuspendUntil) m.unmarshal(getClass().getResourceAsStream("/suspendUntil.xml"));
        XQueryEvaluator ev = new XQueryEvaluator();
        Date d = (Date) ev.evaluateExpression("declare namespace xsd='http://www.w3.org/2001/XMLSchema'; xsd:dateTime('2009-01-01T12:59:34')", null).get(0);
        System.out.println(d);
        e.getTime().getTimePeriod().addTo(d);
        
        Date d2 = (Date) ev.evaluateExpression("declare namespace xsd='http://www.w3.org/2001/XMLSchema'; xsd:dateTime('2009-01-04T12:59:34')", null).get(0);
        System.out.println(d2);
        Assert.assertEquals(d2, d);
        System.out.println(d);
    }
}
