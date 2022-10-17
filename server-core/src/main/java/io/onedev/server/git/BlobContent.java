package io.onedev.server.git;

import java.io.Serializable;

public class BlobContent implements Serializable {

	private static final long serialVersionUID = 1L;

	private final byte[] bytes;
	
	private final int mode;
	
	public BlobContent(byte[] bytes, int mode) {
		this.bytes = bytes;
		this.mode = mode;
	}
	
	public byte[] getBytes() {
		return bytes;
	}

	public int getMode() {
		return mode;
	}
	
}
