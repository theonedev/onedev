package io.onedev.server.plugin.pack.gem;

import org.jspecify.annotations.Nullable;
import java.io.Serializable;
import java.util.Map;

public class GemData implements Serializable {
	private static final long serialVersionUID = 1L;

	private final byte[] metadata;
	
	private final String platform;
	
	private final Map<String, String> sha256BlobHashes;
	
	public GemData(byte[] metadata, @Nullable String platform, Map<String, String> sha256BlobHashes) {
		this.metadata = metadata;
		this.platform = platform;
		this.sha256BlobHashes = sha256BlobHashes;
	}

	public byte[] getMetadata() {
		return metadata;
	}

	@Nullable
	public String getPlatform() {
		return platform;
	}

	public Map<String, String> getSha256BlobHashes() {
		return sha256BlobHashes;
	}
}
