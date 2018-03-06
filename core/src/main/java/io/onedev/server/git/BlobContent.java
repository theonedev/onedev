package io.onedev.server.git;

import java.io.Serializable;

import org.eclipse.jgit.lib.FileMode;

public interface BlobContent extends Serializable {

	byte[] getBytes();
	
	FileMode getMode();
	
	public static class Immutable implements BlobContent {

		private static final long serialVersionUID = 1L;

		private final byte[] bytes;
		
		private final FileMode mode;
		
		public Immutable(byte[] bytes, FileMode mode) {
			this.bytes = bytes;
			this.mode = mode;
		}
		
		@Override
		public byte[] getBytes() {
			return bytes;
		}

		@Override
		public FileMode getMode() {
			return mode;
		}
		
	}
}
