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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.api.HISEUserDetails;
import org.apache.hise.dao.TaskOrgEntity.OrgEntityType;
import org.apache.hise.engine.wsdl.IllegalArgumentFault;
import org.apache.hise.engine.wsdl.IllegalStateFault;
import org.apache.hise.lang.xsd.htd.TTask;
import org.apache.hise.lang.xsd.htda.TStatus;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.JpaCallback;
import org.springframework.orm.jpa.support.JpaDaoSupport;
import org.apache.hise.dao.JpaQueryBuilder.JQBParam;

public class HISEDao extends JpaDaoSupport {

    private static final Log log = LogFactory.getLog(HISEDao.class);

    // /**
    // * Returns tasks. See {@link HumanTaskServices#getMyTasks(String, TaskTypes, GenericHumanRole, String, List, String, String, Integer)}
    // * for method contract.
    // */
    // public List<Task> getTasks(Assignee owner, TaskTypes taskType, GenericHumanRole genericHumanRole, String workQueue, List<Status> statuses,
    // String whereClause, String orderByClause, String createdOnClause, Integer maxTasks, Integer offset) {
    //
    // Map<String, Object> namedParameters = new HashMap<String, Object>();
    //        
    // StringBuilder queryBuilder = new StringBuilder("SELECT t FROM Task t ");
    //        
    // //query for human role must be first
    //        
    // if (genericHumanRole != null) {
    //            
    // switch (genericHumanRole) {
    //            
    // //TODO: owner for now but should be workQueue
    //                
    // case ACTUAL_OWNER:
    // //duplicates statement set when workQueue is null - not an error
    // queryBuilder.append("WHERE t.actualOwner = :owner AND ");
    // namedParameters.put("owner", owner);
    // break;
    //                    
    // case BUSINESS_ADMINISTRATORS:
    // //TODO implement
    // throw new UnsupportedOperationException("Query for BUSINESS_ADMINISTRATORS not implemented.");
    // //break;
    //                    
    // case NOTIFICATION_RECIPIENTS:
    // queryBuilder.append("JOIN t.notificationRecipients ghr WHERE ghr = :owner AND ");
    // namedParameters.put("owner", owner);
    // break;
    //                    
    // case POTENTIAL_OWNERS:
    // queryBuilder.append("JOIN t.potentialOwners ghr WHERE ghr = :owner AND ");
    // namedParameters.put("owner", owner);
    // break;
    //                    
    // case TASK_INITIATOR:
    // //TODO implement
    // throw new UnsupportedOperationException("Query for TASK_INITIATOR not implemented.");
    // //break;
    //                    
    // case TASK_STAKEHOLDERS:
    // queryBuilder.append("JOIN t.taskStakeholders ghr WHERE ghr = :owner AND ");
    // namedParameters.put("owner", owner);
    // break;
    //                    
    // default:
    // }
    //         
    // } else {
    //            
    // queryBuilder.append("WHERE ");
    // }
    //
    // if (workQueue == null) {
    //            
    // //workQueue = owner.toString();
    // //TODO ACTUAL_OWNER.toString() ?
    // queryBuilder.append("t.actualOwner = :actualOwner AND ");
    // namedParameters.put("actualOwner", owner);
    // }
    //        
    // if (statuses != null && !statuses.isEmpty()) {
    //            
    // queryBuilder.append("t.status in (:statuses) AND ");
    // namedParameters.put("statuses", statuses);
    // }
    //        
    // if (createdOnClause != null) {
    //            
    // queryBuilder.append("t.createdOn = :createdOnClause AND ");
    // namedParameters.put("createdOn", createdOnClause);
    // }
    //        
    // if (whereClause != null) {
    //            
    // queryBuilder.append(whereClause);
    // queryBuilder.append(" AND ");
    // }
    //
    // String queryString = queryBuilder.toString();
    //        
    // if (queryString.endsWith(" AND ")) {
    // queryString = queryString.substring(0, queryString.length() - 4);
    // }
    //
    // if (queryString.endsWith(" WHERE ")) {
    // queryString = queryString.substring(0, queryString.length() - 6);
    // }
    //        
    // if (orderByClause != null) {
    //            
    // queryBuilder = new StringBuilder(queryString);
    // queryBuilder.append(" ORDER BY ");
    // queryBuilder.append(orderByClause);
    // queryBuilder.append(" ");
    //            
    // } else {
    //            
    // queryBuilder = new StringBuilder(queryString);
    // queryBuilder.append( " ORDER BY t.activationTime ");
    //            
    // }
    //
    // //TODO extract query building
    // queryString = queryBuilder.toString();
    //        
    // if (log.isDebugEnabled()) {
    // log.debug("query: " + queryString);
    // log.debug("parameters: " + namedParameters);
    // }
    //        
    // Query query = entityManager.createQuery(queryString);
    //        
    // if (maxTasks != null && maxTasks > 0) {
    // //query.setFirstResult(0);
    // query.setMaxResults(maxTasks);
    // }
    //        
    // if (offset != null) {
    // query.setFirstResult(offset);
    // } else {
    // query.setFirstResult(0);
    // }
    //        
    // for (Map.Entry<String, Object> parameter : namedParameters.entrySet()) {
    // query.setParameter(parameter.getKey(), parameter.getValue());
    // }
    //        
    // List<Task> result = query.getResultList();
    // return result;
    // }
    // /**
    // * Checks if given task exists.
    // * @param primaryKey Primary key of the entity
    // * @return true if entity exists false otherwise
    // */
    // public boolean exists(Long id) {
    // try {
    // entityManager.find(Task.class,id);
    // return true;
    // } catch (EntityNotFoundException xENF) {
    // return false;
    // }
    // }
    //    
    // /**
    // * Retrieves domain object from persistent store.
    // * @param id Identifier of requested domain object
    // * @return fetched domain object
    // */
    // public Task fetch(Long id) {
    // return entityManager.find(Task.class, id);
    // }
    //    
    // /**
    // * Saves domain object in persistent store.
    // * @param entity Domain object to save
    // */
    // public void update(Task entity) {
    // entityManager.merge(entity);
    // }
    //    
    // /**
    // * Creates domain object in persistent store.
    // * @param entity Domain object to create
    // */
    // public void create(Task entity) {
    // entityManager.persist(entity);
    // }
    //
    // public List<Task> getTasks(Assignee owner, TaskTypes taskType, GenericHumanRole genericHumanRole, String workQueue, List<Status> status, String whereClause, String orderByClause, String createdOnClause, Integer maxTasks, Integer offset) {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //    
    //    
    // /**
    // * {@inheritDoc}
    // */
    //
    // /**
    // * {@inheritDoc}
    // */
    // public Assignee fetch(Long id) {
    // return entityManager.find(Assignee.class, id);
    // }
    //    
    // /**
    // * {@inheritDoc}
    // */
    // public void update(Assignee entity) {
    // entityManager.merge(entity);
    // }
    //    
    // /**
    // * {@inheritDoc}
    // */
    // public void create(Assignee entity) {
    // entityManager.persist(entity);
    // }
    //
    // /**
    // * {@inheritDoc}
    // */
    // public Group getGroup(String name) {
    //        
    // Query query = entityManager.createQuery("SELECT g FROM Group g WHERE g.name = :name");
    // query.setParameter("name", name);
    //        
    // try {
    //            
    // return (Group) query.getSingleResult();
    // } catch (NoResultException e) {
    //            
    // return null;
    // }
    // }
    //
    // /**
    // * {@inheritDoc}
    // * TODO should work when ids are set
    // */
    // public Set<Assignee> saveNotExistingAssignees(Set<Assignee> assignees) {
    //        
    // Set<Assignee> result = new HashSet<Assignee>();
    //        
    // for (Assignee a : assignees) {
    //
    // if (a instanceof Person) {
    //
    // Person p = this.getPerson(a.getName());
    // if (p == null) {
    // this.create(a);
    // p = (Person) a;
    // }
    // result.add(p);
    //               
    // } else if (a instanceof Group) {
    //                
    // Group g = this.getGroup(a.getName());
    // if (g == null) {
    // this.create(a);
    // g = (Group) a;
    // }
    // result.add(g);
    // }
    // }
    //
    // return result;
    // }
    //    

    
    // public Person loadUser(String userId) {
    // return getJpaTemplate().find(Person.class, userId);
    // }

    public OrgEntity getOrgEntity(final String name) {
        return (OrgEntity) getJpaTemplate().execute(new JpaCallback() {
            public Object doInJpa(EntityManager e) throws PersistenceException {
                Query query = e.createQuery("FROM OrgEntity o WHERE o.name = :name");
                query.setParameter("name", name);
                return query.getSingleResult();
            }
        });
    }
    
    public List<Task> getUserTasks(final TaskQuery query) {
//        TaskOrgEntity to;to.g
        switch (query.getGenericHumanRole()) {
        case ACTUALOWNER:
            return (List<Task>) getJpaTemplate().executeFind(new JpaCallback() {
                public Object doInJpa(EntityManager em) throws PersistenceException {
                    return em.createQuery("select distinct t from Task t where t.actualOwner = :user")
                    .setParameter("user", query.getUser())
                    .setMaxResults(query.getMaxTasks())
                    .getResultList();
                }
            });
        case POTENTIALOWNERS:
            return (List<Task>) getJpaTemplate().executeFind(new JpaCallback() {
                public Object doInJpa(EntityManager em) throws PersistenceException {
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
                            ") and e.genericHumanRole = :role",
                            new JQBParam("role", query.getGenericHumanRole())
                    })
                    .setMaxResults(query.getMaxTasks())
                    .getResultList();
                }
            });
        default:
            throw new IllegalStateException();
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
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException("", e);
        }
    }
    
    public <T> void clearAllRecords(Class<T> clazz) {
        log.debug("select t from " + clazz.getName());
        for (Object o : getJpaTemplate().find("select t from " + clazz.getName() + " t")) {
            getJpaTemplate().remove(o);
        }
    }

}
