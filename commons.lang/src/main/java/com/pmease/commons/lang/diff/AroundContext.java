package com.pmease.commons.lang.diff;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class AroundContext implements Serializable {
	
	private List<DiffBlock> diffs;
	
	private final int line;
	
	private final boolean aboveOmitted;
	
	private final boolean belowOmitted;
	
	public AroundContext(List<DiffBlock> diffs, int line, boolean aboveOmitted, boolean belowOmitted) {
		this.diffs = diffs;
		this.line = line;
		this.aboveOmitted = aboveOmitted;
		this.belowOmitted = belowOmitted;
	}

	public List<DiffBlock> getDiffs() {
		return diffs;
	}

	public void setDiffs(List<DiffBlock> diffs) {
		this.diffs = diffs;
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
	
}
