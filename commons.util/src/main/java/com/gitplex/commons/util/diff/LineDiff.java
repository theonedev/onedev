package com.gitplex.commons.util.diff;

import java.io.Serializable;
import java.util.List;

import com.gitplex.jsyntax.Token;

public class LineDiff implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int compareLine;
	
	private final List<DiffBlock<Token>> tokenDiffs;
	
	public LineDiff(int line, List<DiffBlock<Token>> diffs) {
		this.compareLine = line;
		this.tokenDiffs = diffs;
	}

	public int getCompareLine() {
		return compareLine;
	}

	public List<DiffBlock<Token>> getTokenDiffs() {
		return tokenDiffs;
	}
	
}
