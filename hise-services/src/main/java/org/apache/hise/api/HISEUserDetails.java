package org.apache.hise.api;

import java.util.Collection;

public interface HISEUserDetails {
    public Collection<String> getUserGroups(String user);
    public String getUserPassword(String user);
}
