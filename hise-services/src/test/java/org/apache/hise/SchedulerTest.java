package org.apache.hise;

import org.apache.hise.engine.HISEScheduler;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class SchedulerTest {
    
    @Test
    public void test() throws Exception {
        HISEScheduler s = new HISEScheduler();
        s.init();
        Thread.sleep(30000);
        s.destroy();
    }
}
