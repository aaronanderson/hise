package org.apache.hise.test;

import javax.jws.WebService;

import org.apache.hise.Test;
import org.apache.hise.api.HISEEngine;
import org.apache.hise.dao.Job;
import org.apache.hise.dao.Task;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@WebService(endpointInterface = "org.apache.hise.Test")
public class TestImpl implements Test {
    
    private PlatformTransactionManager transactionManager;
    
    private HISEEngine e;
    
    public void setE(HISEEngine hiseEngine) {
		this.e = hiseEngine;
	}

	public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void cleanup() throws Exception {
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        tt.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
//                hiseDao.clearAllRecords(OrgEntity.class);
                e.getHiseDao().clearAllRecords(Task.class);
                e.getHiseDao().clearAllRecords(Job.class);
                return null;
            }
        });
    }
}
