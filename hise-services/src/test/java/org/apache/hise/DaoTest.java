package org.apache.hise;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hise.dao.GenericHumanRole;
import org.apache.hise.dao.HISEDao;
import org.apache.hise.dao.OrgEntity;
import org.apache.hise.dao.Task;
import org.apache.hise.dao.TaskOrgEntity;
import org.apache.hise.dao.Task.Status;
import org.apache.hise.dao.TaskOrgEntity.OrgEntityType;
import org.apache.hise.lang.xsd.htda.TStatus;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

@ContextConfiguration(locations = "classpath:/dao.xml")
public class DaoTest extends AbstractTransactionalJUnit4SpringContextTests {
    
    @Autowired
    private HISEDao hiseDao;
    
    private OrgEntity o;
    
    private void addTask() throws Exception {
        Assert.assertTrue(hiseDao != null);
        
        o = new OrgEntity();
        o.setName("user1");
        o.setType(OrgEntityType.USER);
        o.setUserPassword("abc");
        hiseDao.saveOrgEntity(o);
        
        Task t = new Task();
        t.setStatus(Status.CREATED);
        t.setTaskDefinitionKey("asd");
        t.setActualOwner(o);
        hiseDao.saveTask(t);
    }

    private void addTask2() throws Exception {
        addTask();
        Task t = new Task();
        t.setStatus(Status.READY);
        t.setTaskDefinitionKey("asd2");
        Set<TaskOrgEntity> pa = new HashSet<TaskOrgEntity>();
        TaskOrgEntity x = new TaskOrgEntity();
        x.setName("user1");
        x.setType(OrgEntityType.USER);
        x.setGenericHumanRole(GenericHumanRole.POTENTIALOWNERS);
        x.setTask(t);
        pa.add(x);
        t.setPeopleAssignments(pa);
        hiseDao.saveTask(t);
    }

    @Test
    public void testDao() throws Exception {
        addTask();
    }
    
    @Test 
    public void testUserTasks() throws Exception {
        addTask();
        List<Task> r = hiseDao.getUserTasks(o, "", GenericHumanRole.ACTUALOWNER, "", Collections.EMPTY_LIST, "", null, 100);
        Assert.assertEquals("asd", r.get(0).getTaskDefinitionKey());
    }

    @Test 
    public void testUserTasks2() throws Exception {
        addTask2();
        List<Task> r = hiseDao.getUserTasks(o, "", GenericHumanRole.POTENTIALOWNERS, "", Collections.EMPTY_LIST, "", null, 100);
        Assert.assertEquals("asd2", r.get(0).getTaskDefinitionKey());
    }
}
