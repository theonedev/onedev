package io.onedev.server.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.io.IOUtils;
import org.jspecify.annotations.Nullable;

public class FileData implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final int MAX_READ_SIZE = 5*1024*1024;

	private final String name;

	private final byte[] content;

	private final long size;

	public FileData(String name, byte[] content, long size) {
		this.name = name;
		this.content = content;
		this.size = size;
	}

	@Nullable
	public static FileData from(File file) throws IOException {
		if (file.exists()) {
			String name = file.getName();
			long length = file.length();
			long toRead = Math.min(length, MAX_READ_SIZE);
			byte[] content = new byte[(int) toRead];
			if (toRead > 0) {
				try (var is = new FileInputStream(file)) {
					IOUtils.readFully(is, content);
				}
			}
			return new FileData(name, content, length);
		} else {
			return null;
		}
	}

	public String getName() {
		return name;
	}

	public byte[] getContent() {
		return content;
	}

	public long getSize() {
		return size;
	}

	public boolean isPartial() {
		return size > content.length;
	}

}
