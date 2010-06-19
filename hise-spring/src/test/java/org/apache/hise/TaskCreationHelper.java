package org.apache.hise;

import java.util.HashSet;
import java.util.Set;

import org.apache.hise.dao.GenericHumanRole;
import org.apache.hise.dao.HISEDao;
import org.apache.hise.dao.Job;
import org.apache.hise.dao.Message;
import org.apache.hise.dao.OrgEntity;
import org.apache.hise.dao.Task;
import org.apache.hise.dao.TaskOrgEntity;
import org.apache.hise.dao.Task.Status;
import org.apache.hise.dao.TaskOrgEntity.OrgEntityType;
import org.junit.Assert;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public class TaskCreationHelper {
	
	public enum TaskType {
		TASK1, TASK2, TASK3
	}
	
	public static String TASK1_GROUP = "group1";
	public static String TASK1_OWNER = "user1";
	public static String TASK1_OWNER_PASSWORD = "abc";
	public static String TASK1_DEFINITION_KEY = "asd";
	public static String TASK1_INPUT_KEY = "abc";
	public static String TASK1_INPUT_VALUE = "def";
	
	public static String TASK2_ASSIGNED_USER = "user1";
	public static String TASK2_ASSIGNED_USER_GROUP = "group1";
	public static String TASK2_DEFINITION_KEY = "asd2";
	
	public static String TASK3_ASSIGNED_USER = "user1";
	public static String TASK3_ASSIGNED_GROUP = "group1";
	public static String TASK3_DEFINITION_KEY = "asd3";
	
	
	private void cleanup(HISEDao hiseDao) throws Exception {
        hiseDao.clearAllRecords(OrgEntity.class);
        hiseDao.clearAllRecords(Task.class);
        hiseDao.clearAllRecords(Job.class);
    }
    
    
    public Long addTask(HISEDao hiseDao) throws Exception {
        Assert.assertTrue(hiseDao != null);
        
        OrgEntity o, o2;
        
        o2 = new OrgEntity();
        o2.setName(TASK1_GROUP);
        o2.setType(OrgEntityType.GROUP);
        hiseDao.persist(o2);

        o = new OrgEntity();
        o.setName(TASK1_OWNER);
        o.setType(OrgEntityType.USER);
        o.setUserPassword(TASK1_OWNER_PASSWORD);
        
        o.addToGroup(o2);
        hiseDao.persist(o);
        
        Task t = new Task();
        t.setStatus(Status.CREATED);
        t.setTaskDefinitionKey(TASK1_DEFINITION_KEY);
        t.setActualOwner(TASK1_OWNER);
        
        t.getInput().put(TASK1_INPUT_KEY, new Message(TASK1_INPUT_KEY, TASK1_INPUT_VALUE));
        hiseDao.persist(t);
        Long id = t.getId();
        Assert.assertNotNull(id);
        return id;
    }

    public void addTask2(HISEDao hiseDao) throws Exception {
        addTask(hiseDao);
        Task t = new Task();
        t.setStatus(Status.READY);
        t.setTaskDefinitionKey(TASK2_DEFINITION_KEY);
        Set<TaskOrgEntity> pa = new HashSet<TaskOrgEntity>();
        TaskOrgEntity x = new TaskOrgEntity();
        x.setName(TASK2_ASSIGNED_USER);
        x.setType(OrgEntityType.USER);
        x.setGenericHumanRole(GenericHumanRole.POTENTIALOWNERS);
        x.setTask(t);
        pa.add(x);
        t.setPeopleAssignments(pa);
        hiseDao.persist(t);
    }

    public void addTask3(HISEDao hiseDao) throws Exception {
        addTask(hiseDao);
        Task t = new Task();
        t.setStatus(Status.READY);
        t.setTaskDefinitionKey(TASK3_DEFINITION_KEY);
        Set<TaskOrgEntity> pa = new HashSet<TaskOrgEntity>();
        TaskOrgEntity x = new TaskOrgEntity();
        x.setName(TASK3_ASSIGNED_GROUP);
        x.setType(OrgEntityType.GROUP);
        x.setGenericHumanRole(GenericHumanRole.POTENTIALOWNERS);
        x.setTask(t);
        pa.add(x);
        t.setPeopleAssignments(pa);
        hiseDao.persist(t);
    }
    
    public Long addTaskInTransaction(final TaskType taskType, TransactionTemplate tt, final HISEDao hiseDao) {
    	Long tid = (Long) tt.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus arg0) {
                try {
                	cleanup(hiseDao);
                	Long tid = null;
                    switch(taskType) {
                    	case TASK1:
                    		tid = addTask(hiseDao);
                    		break;
                    	case TASK2:
                    		addTask2(hiseDao);
                    		break;
                    	case TASK3:
                    		addTask3(hiseDao);
                    		break;
                    }
                    return tid;
                	
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    	return tid;
    }

    
}