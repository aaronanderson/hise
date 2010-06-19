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
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.dao.HISEDao;
import org.apache.hise.dao.Job;
import org.apache.hise.dao.Task;
import org.apache.hise.dao.Transactional;

/**
 * 
 * @author Rafał Rusin
 */
public class HISESchedulerImpl implements HISEScheduler {

    private static final Log log = LogFactory.getLog(HISESchedulerImpl.class);
    @Inject
    private JobExecutor jobExecutor;
    private ScheduledExecutorService executor;

    public static interface JobExecutor extends Runnable {
        public Job createJob(Date when, String action, Task task);
    }

    @Transactional
    public static class JobExecutorImpl implements JobExecutor {

        @Inject
        private HISEDao hiseDao;
        @Inject
        private Provider<JobTask> jobTaskBuilder;

        public void run() {
            final Date currentEventDateTime = Calendar.getInstance().getTime();
            HISESchedulerImpl.log.debug("scheduler CheckJobs at " + currentEventDateTime);
            try {
                List<Job> jobs = hiseDao.listJobs(currentEventDateTime, 50);
                HISESchedulerImpl.log.debug("dequeued jobs: " + jobs);
                for (Job j : jobs) {
                    try {
                        final Long j2 = j.getId();
                        JobTask task = jobTaskBuilder.get();
                        task.execute(j2);
                    } catch (Throwable t) {
                        HISESchedulerImpl.log.warn("Job execution failed " + j, t);
                    }
                }
            } catch (Throwable t) {
                HISESchedulerImpl.log.warn("CheckJobs failed", t);
            }
        }

        public Job createJob(Date when, String action, Task task) {
            Job job = new Job();
            job.setFire(when);
            job.setTask(task);
            job.setAction(action);
            hiseDao.persist(job);
            return job;
        }

        public void setHiseDao(HISEDao hiseDao) {
            this.hiseDao = hiseDao;
        }

        public void setJobTaskBuilder(Provider<JobTask> jobTaskBuilder) {
            this.jobTaskBuilder = jobTaskBuilder;
        }
    }

    public static interface JobTask {
        public void execute(Long jobId);
    }

    @Transactional
    public static class JobTaskImpl implements JobTask {

        @Inject
        private HISEEngineImpl hiseEngine;

        public void execute(Long jobId) {
            Job j3 = hiseEngine.getHiseDao().find(Job.class, jobId);
            if (j3 == null) {
                HISESchedulerImpl.log.debug("Skipping job " + j3 + " - it\'s no longer id DB");
            } else {
                HISESchedulerImpl.log.debug("Executing job " + j3);
                hiseEngine.executeJob(j3);
                hiseEngine.getHiseDao().remove(j3);
            }
        }

        public void setHiseEngine(HISEEngineImpl hiseEngine) {
            this.hiseEngine = hiseEngine;
        }
    }

    @PostConstruct
    public void init() {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(jobExecutor, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    public void setJobExecutor(JobExecutor jobExecutor) {
        this.jobExecutor = jobExecutor;
    }

    public Job createJob(Date when, String action, Task task) {
        return jobExecutor.createJob(when, action, task);
    }

    @PreDestroy
    public void destroy() {
        executor.shutdown();
    }
}
