package com.gitplex.server.git;

import java.io.Serializable;

public interface PathAndContent extends Serializable {

	String getPath();

	byte[] getContent();
	
	public static class Immutable implements PathAndContent {

		private static final long serialVersionUID = 1L;

		private final String path;
		
		private final byte[] content;
		
		public Immutable(String path, byte[] content) {
			this.path = path;
			this.content = content;
		}
		
		@Override
		public String getPath() {
			return path;
		}

		@Override
		public byte[] getContent() {
			return content;
		}
		
	}
}
