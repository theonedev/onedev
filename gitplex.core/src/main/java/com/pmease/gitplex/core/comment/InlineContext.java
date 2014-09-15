package com.pmease.gitplex.core.comment;

import java.io.Serializable;
import java.util.List;

import com.pmease.commons.util.diff.DiffLine;

@SuppressWarnings("serial")
public class InlineContext implements Serializable {
	private final List<DiffLine> diffs;
	
	private final boolean aboveOmitted;
	
	private final boolean belowOmitted;
	
	public InlineContext(List<DiffLine> diffs, boolean aboveOmitted, boolean belowOmitted) {
		this.diffs = diffs;
		this.aboveOmitted = aboveOmitted;
		this.belowOmitted = belowOmitted;
	}

	public List<DiffLine> getDiffs() {
		return diffs;
	}

	public boolean isAboveOmitted() {
		return aboveOmitted;
	}

	public boolean isBelowOmitted() {
		return belowOmitted;
	}
	
}
