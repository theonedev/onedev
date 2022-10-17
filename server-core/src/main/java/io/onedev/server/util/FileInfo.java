package io.onedev.server.util;

import java.io.Serializable;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class FileInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String path;
	
	private final long length;

	private final long lastModified;
	
	public FileInfo(String path, long length, long lastModified) {
		this.path = path;
		this.length = length;
		this.lastModified = lastModified;
	}
	
	public String getPath() {
		return path;
	}

	public long getLength() {
		return length;
	}
	
	public long getLastModified() {
		return lastModified;
	}

	public boolean isFile() {
		return length >= 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof FileInfo) {
			FileInfo otherInfo = (FileInfo) obj;
			return otherInfo.getPath().equals(path);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(path).toHashCode();
	}
	
}
