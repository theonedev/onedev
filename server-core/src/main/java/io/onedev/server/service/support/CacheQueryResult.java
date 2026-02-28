package io.onedev.server.service.support;

import java.io.Serializable;

import io.onedev.k8shelper.CacheAvailability;

public class CacheQueryResult implements Serializable {

    private final Long projectId;

    private final Long cacheId;

    private final boolean exactMatch;

    public CacheQueryResult(Long projectId, Long cacheId, boolean exactMatch) {
        this.projectId = projectId;
        this.cacheId = cacheId;
        this.exactMatch = exactMatch;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getCacheId() {
        return cacheId;
    }

    public boolean isExactMatch() {
        return exactMatch;
    }

    public CacheAvailability getCacheAvailability() {
        return exactMatch ? CacheAvailability.EXACT_MATCH : CacheAvailability.PARTIAL_MATCH;
    }
    
}
