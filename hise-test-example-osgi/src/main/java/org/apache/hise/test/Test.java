package org.apache.hise.test;

import javax.jws.WebService;

@WebService
public interface Test {
    void cleanup() throws Exception;
}
