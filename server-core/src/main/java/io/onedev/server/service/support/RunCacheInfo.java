package io.onedev.server.service.support;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.jspecify.annotations.Nullable;

public class RunCacheInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String key;

    @Nullable
    private final String checksum;

    @Nullable
    private final Date lastAccessDate;

    private final List<PathInfo> indexedPaths;

    public RunCacheInfo(String key, @Nullable String checksum, @Nullable Date lastAccessDate,
                        List<PathInfo> indexedPaths) {
        this.key = key;
        this.checksum = checksum;
        this.lastAccessDate = lastAccessDate;
        this.indexedPaths = indexedPaths;
    }

    public String getKey() {
        return key;
    }

    @Nullable
    public String getChecksum() {
        return checksum;
    }

    @Nullable
    public Date getLastAccessDate() {
        return lastAccessDate;
    }

    public List<PathInfo> getIndexedPaths() {
        return indexedPaths;
    }

}
