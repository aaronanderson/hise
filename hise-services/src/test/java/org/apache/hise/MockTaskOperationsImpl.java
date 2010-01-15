package org.apache.hise;

import org.apache.hise.engine.jaxws.TaskOperationsImpl;

public class MockTaskOperationsImpl extends TaskOperationsImpl {

    @Override
    protected String getUserString() {
        return "user1";
    }

}
