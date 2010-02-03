package org.apache.hise;

import java.util.Date;
import java.util.List;

import org.apache.hise.TaskCreationHelper.TaskType;
import org.apache.hise.dao.GenericHumanRole;
import org.apache.hise.dao.HISEDao;
import org.apache.hise.dao.Job;
import org.apache.hise.dao.OrgEntity;
import org.apache.hise.dao.Task;
import org.apache.hise.dao.TaskQuery;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@ContextConfiguration(locations = "classpath:/dao.xml")
public class DaoTest extends AbstractJUnit4SpringContextTests {
    
	@Autowired
    private HISEDao hiseDao;
    
    @Autowired
    private JpaTransactionManager transactionManager;
    
    private TaskCreationHelper taskHelper = new TaskCreationHelper();
    
    private void cleanup() throws Exception {
        hiseDao.clearAllRecords(OrgEntity.class);
        hiseDao.clearAllRecords(Task.class);
        hiseDao.clearAllRecords(Job.class);
    }
    
    @Test
    public void testDao() throws Exception {
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        Long tid = taskHelper.addTaskInTransaction(TaskType.TASK1, tt, hiseDao);
        Assert.assertNotNull(tid);
    }
    
    @Test 
    public void testUserTasks() throws Exception {
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        taskHelper.addTaskInTransaction(TaskType.TASK1, tt, hiseDao);
        
        tt.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus arg0) {
                try {
                    TaskQuery query = new TaskQuery();
                    query.setUser(TaskCreationHelper.TASK1_OWNER);
                    List<Task> r = hiseDao.getUserTasks(query);
                    Assert.assertEquals(TaskCreationHelper.TASK1_DEFINITION_KEY, r.get(0).getTaskDefinitionKey());
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
        tt.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus arg0) {
                try{
                    cleanup();
                    taskHelper.addTask2(hiseDao);
                    TaskQuery query = new TaskQuery();
                    query.setUser(TaskCreationHelper.TASK2_ASSIGNED_USER);
                    query.setGenericHumanRole(GenericHumanRole.POTENTIALOWNERS);
                    List<Task> r = hiseDao.getUserTasks(query);
                    query.getUserGroups().add(TaskCreationHelper.TASK2_ASSIGNED_USER_GROUP);
                    r = hiseDao.getUserTasks(query);
                    
                    Assert.assertEquals(TaskCreationHelper.TASK2_DEFINITION_KEY, r.get(0).getTaskDefinitionKey());
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
        final Long tid = taskHelper.addTaskInTransaction(TaskType.TASK1, tt, hiseDao);
        tt.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus arg0) {
                try {
                	Task task = hiseDao.find(Task.class, tid);
                	Assert.assertTrue(task.getInput().get(TaskCreationHelper.TASK1_INPUT_KEY).getMessage().equals(TaskCreationHelper.TASK1_INPUT_VALUE));
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
        taskHelper.addTaskInTransaction(TaskType.TASK3, tt, hiseDao);
                
        tt.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus arg0) {
                try {
                    TaskQuery query = new TaskQuery();
                    query.setUser(TaskCreationHelper.TASK3_ASSIGNED_USER);
                    query.getUserGroups().add(TaskCreationHelper.TASK3_ASSIGNED_GROUP);
                    query.setGenericHumanRole(GenericHumanRole.POTENTIALOWNERS);
                    List<Task> r = hiseDao.getUserTasks(query);
                    Assert.assertEquals(TaskCreationHelper.TASK3_DEFINITION_KEY, r.get(0).getTaskDefinitionKey());
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
    
    @Test 
    public void testDeadlines() throws Exception {
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        final Long tid = (Long) tt.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus arg0) {
                try {
                    cleanup();
                    Task t = hiseDao.find(Task.class, taskHelper.addTask(hiseDao));
                    
                    {
                        Job j = new Job();
                        j.setFire(new Date(1213L));
                        j.setTask(t);
                        j.setAction("abc");
                        hiseDao.persist(j);
                        t.getDeadlines().add(j);
                    }
                    {
                        Job j = new Job();
                        j.setFire(new Date(1213L));
                        j.setTask(t);
                        j.setAction("abc2");
                        hiseDao.persist(j);
                        t.getDeadlines().add(j);
                    }
                    return t.getId();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        Integer s = (Integer) tt.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus arg0) {
                try {
                    Task t = hiseDao.find(Task.class, tid);
                    return t.getDeadlines().size();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        Assert.assertEquals(new Integer(2), s);
    }
}
