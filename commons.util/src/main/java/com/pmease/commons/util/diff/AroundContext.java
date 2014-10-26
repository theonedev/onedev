package com.pmease.commons.util.diff;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class AroundContext implements Serializable {
	
	private List<DiffLine> diffs;
	
	private final int line;
	
	private final boolean aboveOmitted;
	
	private final boolean belowOmitted;
	
	public AroundContext(List<DiffLine> diffs, int line, boolean aboveOmitted, boolean belowOmitted) {
		this.diffs = diffs;
		this.line = line;
		this.aboveOmitted = aboveOmitted;
		this.belowOmitted = belowOmitted;
	}

	public List<DiffLine> getDiffs() {
		return diffs;
	}

	public void setDiffs(List<DiffLine> diffs) {
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
