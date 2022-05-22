package io.onedev.server.util.diff;

import java.io.Serializable;
import java.util.List;

public class LineDiff implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int compareLine;
	
	private final List<DiffBlock<String>> diffBlocks;
	
	public LineDiff(int compareLine, List<DiffBlock<String>> diffBlocks) {
		this.compareLine = compareLine;
		this.diffBlocks = diffBlocks;
	}

	public int getCompareLine() {
		return compareLine;
	}

	public List<DiffBlock<String>> getDiffBlocks() {
		return diffBlocks;
	}
	
}
