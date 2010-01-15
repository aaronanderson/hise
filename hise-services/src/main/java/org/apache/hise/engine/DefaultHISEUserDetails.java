package org.apache.hise.engine;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.Validate;
import org.apache.hise.api.HISEUserDetails;
import org.apache.hise.dao.HISEDao;
import org.apache.hise.dao.OrgEntity;
import org.apache.hise.dao.TaskOrgEntity.OrgEntityType;

/**
 * Default implementation serves user details from HISE DAO
 */
public class DefaultHISEUserDetails implements HISEUserDetails {
    private HISEDao hiseDao;
    
    public void setHiseDao(HISEDao hiseDao) {
        this.hiseDao = hiseDao;
    }

    public String getUserPassword(String user1) {
        OrgEntity user = hiseDao.load(OrgEntity.class, user1);
        return user.getUserPassword();
    }
    
    public Collection<String> getUserGroups(String user1) {
        OrgEntity user = hiseDao.load(OrgEntity.class, user1);
        Collection<String> r = new ArrayList<String>();
        for (OrgEntity g : user.getUserGroups()) {
            Validate.isTrue(g.getType() == OrgEntityType.GROUP);
            r.add(g.getName());
        }
        return r;
    }
}
