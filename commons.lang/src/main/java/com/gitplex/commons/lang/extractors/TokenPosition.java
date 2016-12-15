package com.gitplex.commons.lang.extractors;

import java.io.Serializable;

import javax.annotation.Nullable;

import com.gitplex.commons.util.Range;

/**
 * Represent position of token in source file 
 * 
 * @author robin
 *
 */
public class TokenPosition implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final int line;
	
	private final Range range;

	/**
	 * Construct with line and range
	 * 
	 * @param line
	 * 			0-indexed line of the token 
	 * @param range
	 * 			0-indexed character position of the token in the line
	 */
	public TokenPosition(int line, @Nullable Range range) {
		this.line = line;
		this.range = range;
	}
	
	/**
	 * Get line of the token
	 * 
	 * @return
	 * 			0-indexed line of the token
	 */
	public int getLine() {
		return line;
	}

	/**
	 * Get character position of the token in the line
	 * 
	 * @return
	 * 			0-indexed character position of the token in the line
	 */
	@Nullable
	public Range getRange() {
		return range;
	}

}