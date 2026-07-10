package io.onedev.server.plugin.pack.cargo;

import java.io.Serializable;

public class CargoData implements Serializable {

	private static final long serialVersionUID = 1L;

	private final byte[] metadata;

	private final String sha256BlobHash;

	private boolean yanked;

	public CargoData(byte[] metadata, String sha256BlobHash) {
		this.metadata = metadata;
		this.sha256BlobHash = sha256BlobHash;
	}

	public byte[] getMetadata() {
		return metadata;
	}

	public String getSha256BlobHash() {
		return sha256BlobHash;
	}

	public boolean isYanked() {
		return yanked;
	}

	public void setYanked(boolean yanked) {
		this.yanked = yanked;
	}
}
