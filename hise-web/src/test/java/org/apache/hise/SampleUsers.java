package org.apache.hise;

import org.apache.hise.dao.HISEDao;
import org.apache.hise.dao.OrgEntity;
import org.apache.hise.dao.TaskOrgEntity;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public class SampleUsers {
    
    private HISEDao hiseDao;
    private JpaTransactionManager transactionManager;
    
    public void setHiseDao(HISEDao hiseDao) {
        this.hiseDao = hiseDao;
    }

    public void setTransactionManager(JpaTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
    
    private void addUser(String name, String pass) {
        OrgEntity o = new OrgEntity();
        o.setName(name);
        o.setType(TaskOrgEntity.OrgEntityType.USER);
        o.setUserPassword(pass);
        hiseDao.persist(o);
    }

    public void init() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        
        transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus arg0) {
                OrgEntity g = new OrgEntity();
                {
                    g.setName("group1");
                    g.setType(TaskOrgEntity.OrgEntityType.GROUP);
                    hiseDao.persist(g);
                }
                addUser("user1", "user1pass");
                addUser("user2", "user2pass");
                addUser("user5", "user5pass");
                return null;
            }
        });
    }
}
