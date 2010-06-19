package org.apache.hise;

import org.apache.hise.engine.HISESchedulerImpl;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class SchedulerTest {
    
    @Test
    public void test() throws Exception {
        HISESchedulerImpl s = new HISESchedulerImpl();
        s.init();
        Thread.sleep(30000);
        s.destroy();
    }
}
