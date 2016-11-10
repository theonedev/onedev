package com.gitplex.commons.lang.extractors;

import java.io.Serializable;

import javax.annotation.Nullable;

import com.gitplex.commons.util.Range;

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

}