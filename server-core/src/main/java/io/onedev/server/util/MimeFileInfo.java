package io.onedev.server.util;

public class MimeFileInfo extends FileInfo {

	private static final long serialVersionUID = 1L;

	private final String type;
	
	public MimeFileInfo(String path, long length, long lastModified, String type) {
		super(path, length, lastModified);
		this.type = type;
	}

	public String getType() {
		return type;
	}
	
}
