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

package org.apache.hise.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.dao.JpaQueryBuilder.JQBParam;

/**
 * DAO.
 * 
 */
@Transactional
public class HISEDaoImpl implements HISEDao {

    private static final Log log = LogFactory.getLog(HISEDaoImpl.class);

    @PersistenceContext(name="org.apache.hise")
    public EntityManager em;

    public OrgEntity getOrgEntity(final String name) {
                Query query = em.createQuery("FROM OrgEntity o WHERE o.name = :name");
                query.setParameter("name", name);
                return (OrgEntity)query.getSingleResult();
    }
    
    /**
     * Returns tasks presented to user based on TaskQuery criteria.
     * @param query
     * @return
     */
    public List<Task> getUserTasks(final TaskQuery query) {
        
        Validate.notNull(query);
        
        switch (query.getGenericHumanRole()) {
        case ACTUALOWNER:
            return (List<Task>)em.createQuery("select distinct t from Task t where t.actualOwner = :user order by t.id")
                    .setParameter("user", query.getUser())
                    .setMaxResults(query.getMaxTasks())
                    .getResultList();
            
        case POTENTIALOWNERS:
        case BUSINESSADMINISTRATORS:
        case EXCLUDEDOWNERS:
            return (List<Task>) new JpaQueryBuilder().buildQuery(em, 
                            new Object[] {
                            "select distinct t from Task t, TaskOrgEntity e where e.task = t and (e.name = :user and e.type = :constUser",
                            new JQBParam("user", query.getUser()),
                            new JQBParam("constUser", TaskOrgEntity.OrgEntityType.USER),
                            new JQBParam("groups", query.getUserGroups(), 
                                new Object[] {
                                    " or e.name in (:groups) and e.type = :constGroup", 
                                    new JQBParam("constGroup", TaskOrgEntity.OrgEntityType.GROUP)
                                }),
                            ") and e.genericHumanRole = :role order by t.id",
                            new JQBParam("role", query.getGenericHumanRole())
                    })
                    .setMaxResults(query.getMaxTasks())
                    .getResultList();
            
        default:
            throw new IllegalStateException("generic human role not supported");
        }
    }
    
    public List<Job> listJobs(final Date until, final int maxResult) {
        return (List<Job>) em.createQuery("select j from Job j where j.fire < :until order by j.fire")
                .setParameter("until", until)
                .setMaxResults(maxResult)
                .getResultList();
    }

    public <T extends JpaBase> T find(Class<T> what, Object id) {
        return em.find(what, id);
    }
    
    public <T extends JpaBase> void remove(T o) {
        em.remove(o);
        em.flush();
    }

    public <T extends JpaBase> void persist(T o) {
        em.persist(o);
        em.flush();
    }
    
    public <T extends JpaBase> void clearAllRecords(Class<T> clazz) {
        log.debug("select t from " + clazz.getName());
        for (Object o : em.createQuery("select t from " + clazz.getName() + " t").getResultList()) {
            em.remove(o);
        }
    }

}
