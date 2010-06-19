package org.apache.hise.test;

import javax.annotation.PostConstruct;
import org.apache.hise.api.HISEEngine;
import org.apache.hise.dao.OrgEntity;
import org.apache.hise.dao.TaskOrgEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public class SampleUsers {
    
    private HISEEngine hiseEngine;
    private PlatformTransactionManager transactionManager;

    public void setHiseEngine(HISEEngine hiseEngine) {
		this.hiseEngine = hiseEngine;
	}

	public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
    
    private void addUser(String name, String pass) {
        OrgEntity o = new OrgEntity();
        o.setName(name);
        o.setType(TaskOrgEntity.OrgEntityType.USER);
        o.setUserPassword(pass);
        hiseEngine.getHiseDao().persist(o);
    }

    @PostConstruct
    public void init() {
    	try {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        
        transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus arg0) {
                {
                    OrgEntity g = new OrgEntity();
                    g.setName("group1");
                    g.setType(TaskOrgEntity.OrgEntityType.GROUP);
                    hiseEngine.getHiseDao().persist(g);
                }
                OrgEntity someGroup = new OrgEntity();
                {
                	OrgEntity g = someGroup;
                    g.setName("someGroup");
                    g.setType(TaskOrgEntity.OrgEntityType.GROUP);
                    hiseEngine.getHiseDao().persist(g);
                }
                {
                    OrgEntity o = new OrgEntity();
                    o.setName("someUser");
                    o.setType(TaskOrgEntity.OrgEntityType.USER);
                    o.setUserPassword("someUser");
                    o.getUserGroups().add(someGroup);
                    hiseEngine.getHiseDao().persist(o);
                }
                {
                    OrgEntity o = new OrgEntity();
                    o.setName("someUser2");
                    o.setType(TaskOrgEntity.OrgEntityType.USER);
                    o.setUserPassword("someUser2");
                    o.getUserGroups().add(someGroup);
                    hiseEngine.getHiseDao().persist(o);
                }
                {
                    OrgEntity o = new OrgEntity();
                    o.setName("someUser3");
                    o.setType(TaskOrgEntity.OrgEntityType.USER);
                    o.setUserPassword("someUser3");
                    o.getUserGroups().add(someGroup);
                    hiseEngine.getHiseDao().persist(o);
                }
                addUser("user1", "user1pass");
                addUser("user2", "user2pass");
                addUser("user5", "user5pass");
                return null;
            }
        });
    	} catch (RuntimeException e) {
    		e.printStackTrace();
    	}
    }
}
