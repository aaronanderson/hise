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
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.dao.JpaQueryBuilder.JQBParam;
import org.springframework.orm.jpa.JpaCallback;
import org.springframework.orm.jpa.support.JpaDaoSupport;

/**
 * DAO.
 * 
 */
public class HISEDao extends JpaDaoSupport {

    private static final Log log = LogFactory.getLog(HISEDao.class);

    public OrgEntity getOrgEntity(final String name) {
        return (OrgEntity) getJpaTemplate().execute(new JpaCallback() {
            public Object doInJpa(EntityManager em) throws PersistenceException {
                Query query = em.createQuery("FROM OrgEntity o WHERE o.name = :name");
                query.setParameter("name", name);
                return query.getSingleResult();
            }
        });
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
            return (List<Task>) getJpaTemplate().executeFind(new JpaCallback() {
                public Object doInJpa(EntityManager em) throws PersistenceException {
                    
                    Validate.notNull(em);
                    
                    return em.createQuery("select distinct t from Task t where t.actualOwner = :user order by t.id")
                    .setParameter("user", query.getUser())
                    .setMaxResults(query.getMaxTasks())
                    .getResultList();
                }
            });

        case POTENTIALOWNERS:
        case BUSINESSADMINISTRATORS:
        case EXCLUDEDOWNERS:
            return (List<Task>) getJpaTemplate().executeFind(new JpaCallback() {
                public Object doInJpa(EntityManager em) throws PersistenceException {
                    
                    Validate.notNull(em);
                    
                    return new JpaQueryBuilder().buildQuery(em, 
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
                }
            });
            
        default:
            throw new IllegalStateException("generic human role not supported");
        }
    }
    
    public List<Job> listJobs(final Date until, final int maxResult) {
        return (List<Job>) getJpaTemplate().executeFind(new JpaCallback() {
            public Object doInJpa(EntityManager em) throws PersistenceException {
                return em.createQuery("select j from Job j where j.fire < :until order by j.fire")
                .setParameter("until", until)
                .setMaxResults(maxResult)
                .getResultList();
            }
        });
    }

    public <T> T find(Class<T> what, Object id) {
        return getJpaTemplate().find(what, id);
    }
    
    public void remove(Object o) {
        getJpaTemplate().remove(o);
        getJpaTemplate().flush();
    }

    public void persist(Object o) {
        getJpaTemplate().persist(o);
        getJpaTemplate().flush();
    }
    
    public <T> void clearAllRecords(Class<T> clazz) {
        log.debug("select t from " + clazz.getName());
        for (Object o : getJpaTemplate().find("select t from " + clazz.getName() + " t")) {
            getJpaTemplate().remove(o);
        }
    }

}
