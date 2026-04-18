package io.onedev.server.service.support;

import java.io.Serializable;

import io.onedev.k8shelper.CacheAvailability;

public class CacheFindResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long projectId;

    private final String dirName;

    private final int pathIndex;

    private final boolean exactMatch;

    public CacheFindResult(Long projectId, String dirName, int pathIndex, boolean exactMatch) {
        this.projectId = projectId;
        this.dirName = dirName;
        this.pathIndex = pathIndex;
        this.exactMatch = exactMatch;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getDirName() {
        return dirName;
    }

    public int getPathIndex() {
        return pathIndex;
    }

    public boolean isExactMatch() {
        return exactMatch;
    }

    public CacheAvailability getCacheAvailability() {
        return exactMatch ? CacheAvailability.EXACT_MATCH : CacheAvailability.PARTIAL_MATCH;
    }

}
