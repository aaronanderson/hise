package org.apache.hise;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.apache.hise.utils.DOMUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@Ignore
@ContextConfiguration(locations = "classpath:/dao.xml")
public class JPALoadTest extends AbstractJUnit4SpringContextTests {
    private static Log __log = LogFactory.getLog(JPALoadTest.class);
    
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

        TaskOrgEntity o,o2;
        o = new TaskOrgEntity();
        o.setName("group1");
        o.setType(OrgEntityType.GROUP);
        o.setGenericHumanRole(GenericHumanRole.POTENTIALOWNERS);

        o2 = new TaskOrgEntity();
        o2.setName("user1");
        o2.setType(OrgEntityType.USER);
        o2.setGenericHumanRole(GenericHumanRole.POTENTIALOWNERS);

        Task t = new Task();
        t.setStatus(Status.CREATED);
        t.setTaskDefinitionKey("asd");
        t.setActualOwner("user1");
        t.getInput().put("request", new Message("request", "<asdf/>"));
        t.getInput().put("requestHeader", new Message("requestHeader", "<asdf/>"));
        Set<TaskOrgEntity> pa = new HashSet<TaskOrgEntity>();
        o.setTask(t);
        o2.setTask(t);
        pa.add(o);
        pa.add(o2);
        t.setPeopleAssignments(pa);
        
//        Thread.yield();
        hiseDao.persist(t);
        Long id = t.getId();
        Assert.assertNotNull(id);
        return id;
    }

    
    @Test
    public void testLoad() throws Exception {
        final TransactionTemplate tt = new TransactionTemplate(transactionManager);
        tt.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                try {
                    cleanup();
                    return null;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        ExecutorService exec = Executors.newFixedThreadPool(30);
        for (int i = 0; i < 10000; i++) {
            exec.submit(new Runnable() {
                public void run() {
                    final TransactionTemplate tt2 = new TransactionTemplate(transactionManager);
                    final Long tid = (Long) tt2.execute(new TransactionCallback() {
                        public Object doInTransaction(TransactionStatus arg0) {
                            try {
                                return addTask();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                    __log.debug("tid created: " + tid);
                    tt2.execute(new TransactionCallback() {
                        public Object doInTransaction(TransactionStatus arg0) {
                            try {
                                Task t = hiseDao.find(Task.class, tid);
                                t.setStatus(Status.IN_PROGRESS);
                                hiseDao.persist(t);
                                return t;
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                    __log.debug("tid started: " + tid);
                }
            });
        }
        exec.shutdown();
        exec.awaitTermination(60000, TimeUnit.MILLISECONDS);
    }
}
