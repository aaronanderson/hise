package org.apache.hise.engine;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.dao.Job;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public class Scheduler {
    private static Log __log = LogFactory.getLog(Scheduler.class);
    
    private HISEEngine hiseEngine;
    private ScheduledExecutorService executor;
    private PlatformTransactionManager transactionManager;
    
    private class CheckJobs implements Runnable {

        public void run() {
            Date currentEventDateTime = Calendar.getInstance().getTime();
            __log.debug("scheduler CheckJobs at " + currentEventDateTime);
            List<Job> jobs = hiseEngine.getHiseDao().listJobs(currentEventDateTime, 50);
            __log.debug("jobs: " + jobs);

            for (Job j : jobs) {
                try {
                    final Job j2 = j;
                    TransactionTemplate t = new TransactionTemplate(transactionManager);
                    t.execute(new TransactionCallback() {

                        public Object doInTransaction(TransactionStatus ts) {
                            __log.debug("Executing job " + j2);
                            hiseEngine.executeJob(j2);
                            return null;
                        }
                        
                    });
                } catch (Throwable t) {
                    __log.warn("Job execution failed " + j, t);
                }
            }
        }
    }
    
    public void init() {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(new CheckJobs(), 10000, 10000, TimeUnit.MILLISECONDS);
    }
    
    
    public HISEEngine getHiseEngine() {
        return hiseEngine;
    }

    public void setHiseEngine(HISEEngine hiseEngine) {
        this.hiseEngine = hiseEngine;
    }

    public void destroy() {
        executor.shutdown();
    }
}
