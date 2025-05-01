package io.onedev.server.plugin.pack.helm;

import java.io.Serializable;
import java.util.Map;

public class HelmData implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private final Map<String, Object> metadata;

    private final String sha256Hash;

    public HelmData(Map<String, Object> metadata, String sha256Hash) {
        this.metadata = metadata;
        this.sha256Hash = sha256Hash;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public String getSha256Hash() {
        return sha256Hash;
    }
    
}