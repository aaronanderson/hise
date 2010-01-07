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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hise.dao.Task.Status;
import org.apache.hise.dao.Task.TaskTypes;
import org.springframework.stereotype.Repository;


/**
 * Implements basic JPA DAO for Task {@link Task} and convenience search
 * methods.
 * 
 * @author Witek Wołejszo
 * @author Warren Crossing
 */
@Repository
public class TaskDao {
    
    private static final Log log = LogFactory.getLog(TaskDao.class);

    @PersistenceContext(name = "HISE-PU")
    protected EntityManager entityManager;

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;
    
//    /**
//     * Returns tasks. See {@link HumanTaskServices#getMyTasks(String, TaskTypes, GenericHumanRole, String, List, String, String, Integer)}
//     * for method contract.
//     */
//    public List<Task> getTasks(Assignee owner, TaskTypes taskType, GenericHumanRole genericHumanRole, String workQueue, List<Status> statuses,
//            String whereClause, String orderByClause, String createdOnClause, Integer maxTasks, Integer offset) {
//
//        Map<String, Object> namedParameters = new HashMap<String, Object>();
//        
//        StringBuilder queryBuilder = new StringBuilder("SELECT t FROM Task t ");
//        
//        //query for human role must be first
//        
//        if (genericHumanRole != null) {
//            
//            switch (genericHumanRole) {
//            
//                //TODO: owner for now but should be workQueue 
//                
//                case ACTUAL_OWNER:
//                    //duplicates statement set when workQueue is null - not an error 
//                    queryBuilder.append("WHERE t.actualOwner = :owner AND ");
//                    namedParameters.put("owner", owner);
//                    break;
//                    
//                case BUSINESS_ADMINISTRATORS:
//                    //TODO implement
//                    throw new UnsupportedOperationException("Query for BUSINESS_ADMINISTRATORS not implemented.");
//                    //break;
//                    
//                case NOTIFICATION_RECIPIENTS:
//                    queryBuilder.append("JOIN t.notificationRecipients ghr WHERE ghr = :owner AND ");
//                    namedParameters.put("owner", owner);
//                    break;
//                    
//                case POTENTIAL_OWNERS:
//                    queryBuilder.append("JOIN t.potentialOwners ghr WHERE ghr = :owner AND ");
//                    namedParameters.put("owner", owner);
//                    break;
//                    
//                case TASK_INITIATOR:
//                    //TODO implement
//                    throw new UnsupportedOperationException("Query for TASK_INITIATOR not implemented.");
//                    //break;
//                    
//                case TASK_STAKEHOLDERS:
//                    queryBuilder.append("JOIN t.taskStakeholders ghr WHERE ghr = :owner AND ");
//                    namedParameters.put("owner", owner);
//                    break;
//                    
//                default:
//            }
//         
//        } else {
//            
//            queryBuilder.append("WHERE ");
//        }
//
//        if (workQueue == null) {
//            
//            //workQueue = owner.toString();
//            //TODO ACTUAL_OWNER.toString() ?
//            queryBuilder.append("t.actualOwner = :actualOwner AND ");
//            namedParameters.put("actualOwner", owner);
//        }
//        
//        if (statuses != null && !statuses.isEmpty()) {
//            
//            queryBuilder.append("t.status in (:statuses) AND ");
//            namedParameters.put("statuses", statuses);
//        }
//        
//        if (createdOnClause != null) {
//            
//            queryBuilder.append("t.createdOn = :createdOnClause AND ");
//            namedParameters.put("createdOn", createdOnClause);
//        }
//        
//        if (whereClause != null) {
//            
//            queryBuilder.append(whereClause);
//            queryBuilder.append(" AND ");
//        }        
//
//        String queryString = queryBuilder.toString();
//        
//        if (queryString.endsWith(" AND ")) {
//            queryString = queryString.substring(0, queryString.length() - 4);
//        }
//
//        if (queryString.endsWith(" WHERE ")) {
//            queryString = queryString.substring(0, queryString.length() - 6);
//        }
//        
//        if (orderByClause != null) {
//            
//            queryBuilder = new StringBuilder(queryString);
//            queryBuilder.append(" ORDER BY ");
//            queryBuilder.append(orderByClause);
//            queryBuilder.append(" ");
//            
//        } else {
//            
//            queryBuilder = new StringBuilder(queryString);
//            queryBuilder.append( " ORDER BY t.activationTime ");
//            
//        }
//
//        //TODO extract query building
//        queryString = queryBuilder.toString();
//        
//        if (log.isDebugEnabled()) {
//            log.debug("query: " + queryString);
//            log.debug("parameters: " + namedParameters);
//        }
//        
//        Query query = entityManager.createQuery(queryString);
//        
//        if (maxTasks != null && maxTasks > 0) {
//            //query.setFirstResult(0);
//            query.setMaxResults(maxTasks);
//        }
//        
//        if (offset != null) {
//            query.setFirstResult(offset);
//        } else {
//            query.setFirstResult(0);
//        }
//        
//        for (Map.Entry<String, Object> parameter : namedParameters.entrySet()) {
//            query.setParameter(parameter.getKey(), parameter.getValue());
//        }
//        
//        List<Task> result = query.getResultList();
//        return result;
//    }

    
    /**
     * Checks if given task exists.
     * @param primaryKey Primary key of the entity
     * @return true if entity exists false otherwise
     */
    public boolean exists(Long id) {
        try {
            entityManager.find(Task.class,id);
            return true;
        } catch (EntityNotFoundException xENF) {
            return false;
        }
    }
    
    /**
     * Retrieves domain object from persistent store.
     * @param id Identifier of requested domain object
     * @return fetched domain object
     */
    public Task fetch(Long id) {
        return entityManager.find(Task.class, id);
    }
    
    /**
     * Saves domain object in persistent store. 
     * @param entity Domain object to save
     */
    public void update(Task entity) {
        entityManager.merge(entity);
    }
    
    /**
     * Creates domain object in persistent store. 
     * @param entity Domain object to create
     */
    public void create(Task entity) {
        entityManager.persist(entity);
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public List<Task> getTasks(Assignee owner, TaskTypes taskType, GenericHumanRole genericHumanRole, String workQueue, List<Status> status, String whereClause, String orderByClause, String createdOnClause, Integer maxTasks, Integer offset) {
        // TODO Auto-generated method stub
        return null;
    }
}
