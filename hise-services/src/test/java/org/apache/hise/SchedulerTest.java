package org.apache.hise;

import org.apache.hise.engine.Scheduler;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class SchedulerTest {
    
    @Test
    public void test() throws Exception {
        Scheduler s = new Scheduler();
        s.init();
        Thread.sleep(30000);
        s.destroy();
    }
}
