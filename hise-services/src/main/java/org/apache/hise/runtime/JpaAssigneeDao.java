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

package org.apache.hise.runtime;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;


/**
 * Implements basic JPA DAO for {@link Assignee} and convenience search
 * methods.
 *
 * @author Witek Wołejszo
 */
@Repository
public class JpaAssigneeDao implements AssigneeDao {
    
    private final Log log = LogFactory.getLog(AssigneeDao.class);

    @PersistenceContext(name = "HISE-PU")
    protected EntityManager entityManager;
    
    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;
    
    /**
     * {@inheritDoc}
     */
    public Person getPerson(String name) {

        Query query = entityManager.createQuery("SELECT p FROM Person p WHERE p.name = :name");
        query.setParameter("name", name);
        
        try {
            
            return (Person) query.getSingleResult();
        } catch (NoResultException e) {
            
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Assignee fetch(Long id) {
        return entityManager.find(Assignee.class, id);
    }
    
    /**
     * {@inheritDoc}
     */
    public void update(Assignee entity) {
        entityManager.merge(entity);
    }
    
    /**
     * {@inheritDoc}
     */
    public void create(Assignee entity) {
        entityManager.persist(entity);
    }

    /**
     * {@inheritDoc}
     */
    public Group getGroup(String name) {
        
        Query query = entityManager.createQuery("SELECT g FROM Group g WHERE g.name = :name");
        query.setParameter("name", name);
        
        try {
            
            return (Group) query.getSingleResult();
        } catch (NoResultException e) {
            
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * TODO should work when ids are set
     */
    public Set<Assignee> saveNotExistingAssignees(Set<Assignee> assignees) {
        
        Set<Assignee> result = new HashSet<Assignee>();
        
        for (Assignee a : assignees) {

            if (a instanceof Person) {

                Person p = this.getPerson(a.getName());
                if (p == null) {
                    this.create(a);
                    p = (Person) a;
                }
                result.add(p);
               
            } else if (a instanceof Group) {
                
                Group g = this.getGroup(a.getName());
                if (g == null) {
                    this.create(a);
                    g = (Group) a;
                }
                result.add(g);      
            }
        }

        return result;
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

}
