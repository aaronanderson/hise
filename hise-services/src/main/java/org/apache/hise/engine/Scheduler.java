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
import org.apache.hise.dao.Task;
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
            final Date currentEventDateTime = Calendar.getInstance().getTime();
            __log.debug("scheduler CheckJobs at " + currentEventDateTime);
            TransactionTemplate tt = new TransactionTemplate(transactionManager);
            List<Job> jobs = (List<Job>) tt.execute(new TransactionCallback() {
                public Object doInTransaction(TransactionStatus ts) {
                    return hiseEngine.getHiseDao().listJobs(currentEventDateTime, 50);
                }
            });
            
            __log.debug("dequeued jobs: " + jobs);

            for (Job j : jobs) {
                try {
                    final Job j2 = j;
                    tt.execute(new TransactionCallback() {

                        public Object doInTransaction(TransactionStatus ts) {
                            if (hiseEngine.getHiseDao().load(Job.class, j2.getId()) == null) {
                                __log.debug("Skipping job " + j2 + " - it's no longer id DB");
                            } else {
                                __log.debug("Executing job " + j2);
                                hiseEngine.executeJob(j2);
                                hiseEngine.getHiseDao().remove(j2);
                            }
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
        executor.scheduleWithFixedDelay(new CheckJobs(), 1000, 1000, TimeUnit.MILLISECONDS);
    }
    
    
    public HISEEngine getHiseEngine() {
        return hiseEngine;
    }

    public void setHiseEngine(HISEEngine hiseEngine) {
        this.hiseEngine = hiseEngine;
    }
    
    public Job createJob(Date when, String action, Task task) {
        Job job = new Job();
        job.setFire(when);
        job.setTask(task);
        
        return job;
    }
    

    public void destroy() {
        executor.shutdown();
    }
}
