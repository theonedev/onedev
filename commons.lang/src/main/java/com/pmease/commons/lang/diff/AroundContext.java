package com.pmease.commons.lang.diff;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class AroundContext implements Serializable {
	
	private List<DiffLine> diffLines;
	
	private final int line;
	
	private final boolean aboveOmitted;
	
	private final boolean belowOmitted;
	
	public AroundContext(List<DiffLine> diffLines, int line, boolean aboveOmitted, boolean belowOmitted) {
		this.diffLines = diffLines;
		this.line = line;
		this.aboveOmitted = aboveOmitted;
		this.belowOmitted = belowOmitted;
	}

	public List<DiffLine> getDiffLines() {
		return diffLines;
	}

	public void setDiffLines(List<DiffLine> diffLines) {
		this.diffLines = diffLines;
	}

	public int getLine() {
		return line;
	}

	public boolean isAboveOmitted() {
		return aboveOmitted;
	}

	public boolean isBelowOmitted() {
		return belowOmitted;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (DiffLine diffLine: diffLines)
			buffer.append(diffLine).append("\n");
		return buffer.toString();
	}
	
}
