package io.onedev.server.plugin.pack.pypi;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class PypiData implements Serializable {
	private static final long serialVersionUID = 1L;

	private final Map<String, List<String>> attributes;
	
	private final Map<String, String> sha256BlobHashes;
	
	public PypiData(Map<String, List<String>> attributes, Map<String, String> sha256BlobHashes) {
		this.attributes = attributes;
		this.sha256BlobHashes = sha256BlobHashes;
	}

	public Map<String, List<String>> getAttributes() {
		return attributes;
	}

	public Map<String, String> getSha256BlobHashes() {
		return sha256BlobHashes;
	}
}
