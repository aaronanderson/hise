/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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

/**
 * 
 * @author Rafał Rusin
 */
public class HISEScheduler {
    
    private Log __log = LogFactory.getLog(HISEScheduler.class);
    
    private HISEEngineImpl hiseEngine;
    private ScheduledExecutorService executor;
    private PlatformTransactionManager transactionManager;
    
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    private class CheckJobs implements Runnable {

        public void run() {
            final Date currentEventDateTime = Calendar.getInstance().getTime();
            __log.debug("scheduler CheckJobs at " + currentEventDateTime);
            try {
                TransactionTemplate tt = new TransactionTemplate(transactionManager);
                List<Job> jobs = (List<Job>) tt.execute(new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus ts) {
                        return hiseEngine.getHiseDao().listJobs(currentEventDateTime, 50);
                    }
                });
                
                __log.debug("dequeued jobs: " + jobs);
    
                for (Job j : jobs) {
                    try {
                        final Long j2 = j.getId();
                        tt.execute(new TransactionCallback() {
    
                            public Object doInTransaction(TransactionStatus ts) {
                                Job j3 = hiseEngine.getHiseDao().find(Job.class, j2); 
                                if (j3 == null) {
                                    __log.debug("Skipping job " + j3 + " - it's no longer id DB");
                                } else {
                                    __log.debug("Executing job " + j3);
                                    hiseEngine.executeJob(j3);
                                    hiseEngine.getHiseDao().remove(j3);
                                }
                                return null;
                            }
                            
                        });
                    } catch (Throwable t) {
                        __log.warn("Job execution failed " + j, t);
                    }
                }
            } catch (Throwable t) {
                __log.warn("CheckJobs failed", t);
            }
        }
    }
    
    public void init() {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(new CheckJobs(), 1000, 1000, TimeUnit.MILLISECONDS);
    }
    
    
    public HISEEngineImpl getHiseEngine() {
        return hiseEngine;
    }

    public void setHiseEngine(HISEEngineImpl hiseEngine) {
        this.hiseEngine = hiseEngine;
    }
    
    public Job createJob(Date when, String action, Task task) {
        Job job = new Job();
        job.setFire(when);
        job.setTask(task);
        job.setAction(action);
        hiseEngine.getHiseDao().persist(job);
        return job;
    }
    

    public void destroy() {
        executor.shutdown();
    }
}
