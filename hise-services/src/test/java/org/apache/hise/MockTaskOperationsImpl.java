package org.apache.hise;

import org.apache.hise.dao.OrgEntity;
import org.apache.hise.dao.TaskOrgEntity.OrgEntityType;
import org.apache.hise.engine.jaxws.TaskOperationsImpl;

public class MockTaskOperationsImpl extends TaskOperationsImpl {

    @Override
    protected String getUserString() {
        return "user1";
    }

    @Override
    protected OrgEntity loadUser() {
        OrgEntity oe = new OrgEntity();
        oe.setName("user1");
        oe.setType(OrgEntityType.USER);
        return oe;
    }

}
