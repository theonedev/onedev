package com.pmease.commons.lang.diff;

import java.io.Serializable;
import java.util.List;

import com.pmease.commons.lang.tokenizers.CmToken;

public class LineDiff implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int compareLine;
	
	private final List<DiffBlock<CmToken>> tokenDiffs;
	
	public LineDiff(int line, List<DiffBlock<CmToken>> diffs) {
		this.compareLine = line;
		this.tokenDiffs = diffs;
	}

	public int getCompareLine() {
		return compareLine;
	}

	public List<DiffBlock<CmToken>> getTokenDiffs() {
		return tokenDiffs;
	}
	
}
