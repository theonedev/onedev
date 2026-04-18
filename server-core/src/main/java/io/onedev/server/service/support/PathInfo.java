package io.onedev.server.service.support;

import java.io.Serializable;

public class PathInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String path;

    private final long size;

    public PathInfo(String path, long size) {
        this.path = path;
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

}
