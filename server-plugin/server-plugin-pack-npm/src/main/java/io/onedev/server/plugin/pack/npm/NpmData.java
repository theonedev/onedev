package io.onedev.server.plugin.pack.npm;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

public class NpmData implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final byte[] packageMetadata;

	private final byte[] metadata;
	
	private final Set<String> distTags;

	// Null for packages serialized before tag update dates were recorded.
	private Map<String, Date> distTagUpdateDates;
	
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

	@Nullable
	public Date getDistTagUpdateDate(String tag) {
		return distTagUpdateDates != null ? distTagUpdateDates.get(tag) : null;
	}

	public void setDistTag(String tag, Date updateDate) {
		distTags.add(tag);
		if (distTagUpdateDates == null)
			distTagUpdateDates = new HashMap<>();
		distTagUpdateDates.put(tag, updateDate);
	}

	public void removeDistTag(String tag) {
		distTags.remove(tag);
		if (distTagUpdateDates != null)
			distTagUpdateDates.remove(tag);
	}

	public String getFileName() {
		return fileName;
	}

	public String getFileSha256BlobHash() {
		return fileSha256BlobHash;
	}
}
