package org.apache.hise;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.apache.hise.engine.HISEEngine;
import org.apache.hise.engine.jaxws.TaskOperationsImpl;
import org.apache.hise.lang.xsd.htda.TTask;
import org.junit.Test;

public class TaskOperationsTest {

    @Test
    public void testGetMyTasks() throws Exception {
        TaskOperationsImpl ti = new MockTaskOperationsImpl();
        HISEEngine he = new HISEEngine();
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
}
