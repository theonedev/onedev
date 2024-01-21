package io.onedev.server.plugin.pack.maven;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class MavenData implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final Map<String, String> sha256BlobHashes = new LinkedHashMap<>();

	public Map<String, String> getSha256BlobHashes() {
		return sha256BlobHashes;
	}
	
}
