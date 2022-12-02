package io.onedev.server.util;

public class MimeFileInfo extends FileInfo {

	private static final long serialVersionUID = 1L;

	private final String mediaType;
	
	public MimeFileInfo(String path, long length, long lastModified, String mediaType) {
		super(path, length, lastModified);
		this.mediaType = mediaType;
	}

	public String getMediaType() {
		return mediaType;
	}
	
}
