package org.apache.hise;

import org.apache.hise.dao.HISEDao;
import org.apache.hise.dao.Task;
import org.apache.hise.dao.Task.Status;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

@ContextConfiguration(locations = "classpath:/dao.xml")
public class DaoTest extends AbstractTransactionalJUnit4SpringContextTests {
    
    @Autowired
    private HISEDao hiseDao;
    
    @Test
    public void testDao() throws Exception {
        Assert.assertTrue(hiseDao != null);
        Task t = new Task();
        t.setStatus(Status.CREATED);
        t.setTaskDefinitionKey("asd");
        hiseDao.saveTask(t);
    }
}
