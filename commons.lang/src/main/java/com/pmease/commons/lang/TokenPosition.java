package com.pmease.commons.lang;

import java.io.Serializable;

import javax.annotation.Nullable;

public class TokenPosition implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final int line;
	
	private final Range range;

	public TokenPosition(int line, @Nullable Range range) {
		this.line = line;
		this.range = range;
	}
	
	public int getLine() {
		return line;
	}

	@Nullable
	public Range getRange() {
		return range;
	}

	public static class Range implements Serializable {
		
		private static final long serialVersionUID = 1L;

		private final int start;
		
		private final int end;
		
		public Range(int start, int end) {
			this.start = start;
			this.end = end;
		}

		public int getStart() {
			return start;
		}

		public int getEnd() {
			return end;
		}
		
	}
}