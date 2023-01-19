package io.onedev.server.util.artifact;

import javax.annotation.Nullable;

public class FileInfo extends ArtifactInfo {

	private static final long serialVersionUID = 1L;
	
	private final long length;
	
	private final String mediaType;
	
	public FileInfo(@Nullable String path, long lastModified, long length, @Nullable String mediaType) {
		super(path, lastModified);
		this.length = length;
		this.mediaType = mediaType;
	}

	public long getLength() {
		return length;
	}

	@Nullable
	public String getMediaType() {
		return mediaType;
	}
}
