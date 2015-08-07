package com.pmease.commons.lang.diff;

import java.io.Serializable;
import java.util.List;

public class LineModification implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int compareLine;
	
	private final List<TokenDiffBlock> tokenDiffs;
	
	public LineModification(int line, List<TokenDiffBlock> diffs) {
		this.compareLine = line;
		this.tokenDiffs = diffs;
	}

	public int getCompareLine() {
		return compareLine;
	}

	public List<TokenDiffBlock> getTokenDiffs() {
		return tokenDiffs;
	}
	
}
