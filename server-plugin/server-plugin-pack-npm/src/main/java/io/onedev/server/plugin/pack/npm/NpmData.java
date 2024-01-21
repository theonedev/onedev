package io.onedev.server.plugin.pack.npm;

import java.io.Serializable;
import java.util.Set;

public class NpmData implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final byte[] packageMetadata;

	private final byte[] metadata;
	
	private final Set<String> distTags;
	
	private final String fileName;
	
	private final String fileSha256BlobHash;
	
	public NpmData(byte[] packageMetadata, byte[] metadata, 
				   Set<String> distTags, String fileName, String fileSha256BlobHash) {
		this.packageMetadata = packageMetadata;
		this.metadata = metadata;
		this.distTags = distTags;
		this.fileName = fileName;
		this.fileSha256BlobHash = fileSha256BlobHash;
	}

	public byte[] getPackageMetadata() {
		return packageMetadata;
	}

	public byte[] getMetadata() {
		return metadata;
	}

	public Set<String> getDistTags() {
		return distTags;
	}

	public String getFileName() {
		return fileName;
	}

	public String getFileSha256BlobHash() {
		return fileSha256BlobHash;
	}
}
