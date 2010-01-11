package org.apache.hise;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.hise.dao.GenericHumanRole;
import org.apache.hise.dao.HISEDao;
import org.apache.hise.dao.OrgEntity;
import org.apache.hise.dao.Task;
import org.apache.hise.dao.Task.Status;
import org.apache.hise.lang.xsd.htda.TStatus;

public class MockHiseDao extends HISEDao {

    @Override
    public List<Task> getUserTasks(OrgEntity user, String taskType, GenericHumanRole genericHumanRole, String workQueue, List<TStatus> status, String whereClause, String createdOnClause, Integer maxTasks) {
        Task t = new Task();
        t.setId(123L);
        t.setTaskDefinitionKey("{asdf}asdf");
        t.setStatus(Status.CREATED);
        t.setCreatedOn(new Date(1234L));
        return Collections.singletonList(t);
    }
    
}
