package io.onedev.server.ee.pack.maven;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MavenData implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final Map<String, String> sha256BlobHashes = new HashMap<>();

	public Map<String, String> getSha256BlobHashes() {
		return sha256BlobHashes;
	}
	
}
