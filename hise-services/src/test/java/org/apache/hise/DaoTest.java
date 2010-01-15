package org.apache.hise;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hise.dao.GenericHumanRole;
import org.apache.hise.dao.HISEDao;
import org.apache.hise.dao.Job;
import org.apache.hise.dao.Message;
import org.apache.hise.dao.OrgEntity;
import org.apache.hise.dao.Task;
import org.apache.hise.dao.TaskOrgEntity;
import org.apache.hise.dao.TaskQuery;
import org.apache.hise.dao.Task.Status;
import org.apache.hise.dao.TaskOrgEntity.OrgEntityType;
import org.apache.hise.lang.xsd.htda.TStatus;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@ContextConfiguration(locations = "classpath:/dao.xml")
public class DaoTest extends AbstractJUnit4SpringContextTests {
    
    @Autowired
    private HISEDao hiseDao;
    
    @Autowired
    private JpaTransactionManager transactionManager;
    
    private void cleanup() throws Exception {
        hiseDao.clearAllRecords(OrgEntity.class);
        hiseDao.clearAllRecords(Task.class);
        hiseDao.clearAllRecords(Job.class);
    }
    
    private Long addTask() throws Exception {
        Assert.assertTrue(hiseDao != null);
        
        OrgEntity o, o2;
        
        o2 = new OrgEntity();
        o2.setName("group1");
        o2.setType(OrgEntityType.GROUP);
        hiseDao.persist(o2);

        o = new OrgEntity();
        o.setName("user1");
        o.setType(OrgEntityType.USER);
        o.setUserPassword("abc");
        
        o.addToGroup(o2);
        hiseDao.persist(o);
        
        Task t = new Task();
        t.setStatus(Status.CREATED);
        t.setTaskDefinitionKey("asd");
        t.setActualOwner("user1");
        
        t.getInput().put("abc", new Message("abc", "def"));
        hiseDao.persist(t);
        Long id = t.getId();
        Assert.assertNotNull(id);
        return id;
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
        hiseDao.persist(t);
    }

    private void addTask3() throws Exception {
        addTask();
        Task t = new Task();
        t.setStatus(Status.READY);
        t.setTaskDefinitionKey("asd3");
        Set<TaskOrgEntity> pa = new HashSet<TaskOrgEntity>();
        TaskOrgEntity x = new TaskOrgEntity();
        x.setName("group1");
        x.setType(OrgEntityType.GROUP);
        x.setGenericHumanRole(GenericHumanRole.POTENTIALOWNERS);
        x.setTask(t);
        pa.add(x);
        t.setPeopleAssignments(pa);
        hiseDao.persist(t);
    }

    
    @Test
    public void testDao() throws Exception {
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        final Long tid = (Long) tt.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus arg0) {
                try {
                    return addTask();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    
    
    
    @Test 
    public void testUserTasks() throws Exception {
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        final Long tid = (Long) tt.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus arg0) {
                try {
                    cleanup();
                    return addTask();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        tt.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus arg0) {
                try {
                    TaskQuery query = new TaskQuery();
                    query.setUser("user1");
                    List<Task> r = hiseDao.getUserTasks(query);
                    Assert.assertEquals("asd", r.get(0).getTaskDefinitionKey());
                    return null;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Test 
    public void testUserTasks2() throws Exception {
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        final Long tid = (Long) tt.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus arg0) {
                try{
                    cleanup();
                    addTask2();
                    TaskQuery query = new TaskQuery();
                    query.setUser("user1");
                    query.setGenericHumanRole(GenericHumanRole.POTENTIALOWNERS);
                    List<Task> r = hiseDao.getUserTasks(query);
                    query.getUserGroups().add("group1");
                    r = hiseDao.getUserTasks(query);
                    
                    Assert.assertEquals("asd2", r.get(0).getTaskDefinitionKey());
                    return null;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    
    @Test
    public void testInputs() throws Exception {
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        final Long tid = (Long) tt.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus arg0) {
                try {
                    cleanup();
                    return addTask();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        tt.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus arg0) {
                try {
                    Assert.assertTrue(hiseDao.find(Task.class, tid).getInput().get("abc").getMessage().equals("def"));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });
        
    }
    
    @Test 
    public void testGroupQuery() throws Exception {
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        tt.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus arg0) {
                try {
                    cleanup();
                    addTask3();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });
                
        tt.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus arg0) {
                try {
                    TaskQuery query = new TaskQuery();
                    query.setUser("user1");
                    query.getUserGroups().add("group1");
                    query.setGenericHumanRole(GenericHumanRole.POTENTIALOWNERS);
                    List<Task> r = hiseDao.getUserTasks(query);
                    Assert.assertEquals("asd3", r.get(0).getTaskDefinitionKey());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });
    }

    @Test 
    public void testJobs() throws Exception {
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        tt.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus arg0) {
                try {
                    cleanup();
                    Job j = new Job();
                    j.setFire(new Date(1213L));
                    j.setAction("abc");
                    hiseDao.persist(j);
                    
                    List<Job> r = hiseDao.listJobs(new Date(1214L), 12);
                    Assert.assertEquals("abc", r.get(0).getAction());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });
    }
}
