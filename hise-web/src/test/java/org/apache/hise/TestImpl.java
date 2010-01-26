package org.apache.hise;

import javax.jws.WebService;

import org.apache.hise.dao.HISEDao;
import org.apache.hise.dao.Job;
import org.apache.hise.dao.OrgEntity;
import org.apache.hise.dao.Task;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@WebService(endpointInterface = "org.apache.hise.Test")
public class TestImpl implements Test {
    
    private HISEDao hiseDao;
    private PlatformTransactionManager transactionManager;
    
    public void setHiseDao(HISEDao hiseDao) {
        this.hiseDao = hiseDao;
    }
    
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void cleanup() throws Exception {
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        tt.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
//                hiseDao.clearAllRecords(OrgEntity.class);
                hiseDao.clearAllRecords(Task.class);
                hiseDao.clearAllRecords(Job.class);
                return null;
            }
        });
    }
}
