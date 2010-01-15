package org.apache.hise.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

public class JpaQueryBuilder {
    public static class JQBParam {
        public final String name;
        public final Object value;
        public final Object node;
        public JQBParam(String name, Object value, Object node) {
            super();
            this.name = name;
            this.value = value;
            this.node = node;
        }
        public JQBParam(String name, Object value) {
            this(name, value, null);
        }
    }
    
    private StringBuilder queryString = new StringBuilder();
    private List<JQBParam> params = new ArrayList<JQBParam>();

    private boolean isEmptyValue(Object value) {
        return value == null || value instanceof Collection && ((Collection) value).isEmpty();
    }
    
    private void buildQueryStr(Object node) {
        if (node == null) {
        } else if (node instanceof Object[]) {
            for (Object o : (Object[]) node) {
                buildQueryStr(o);
            }
        } else if (node instanceof JQBParam) {
            JQBParam p = (JQBParam) node;
            if (!isEmptyValue(p.value)) {
                params.add((JQBParam) node);
                buildQueryStr(p.node);
            }
        } else {
            queryString.append(node.toString());
        }
    }
    
    public Query buildQuery(EntityManager em, Object node) {
        buildQueryStr(node);
        Query q = em.createQuery(queryString.toString());
        for (JQBParam p : params) {
            q = q.setParameter(p.name, p.value);
        }
        return q;
    }
}
