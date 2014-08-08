package com.pmease.gitplex.web.component.diff;

import java.io.Serializable;

import com.pmease.commons.util.diff.DiffLine;

@SuppressWarnings("serial")
class HunkLine implements Serializable {

	private int originalLineNo;
	
	private int revisedLineNo;
	
	private DiffLine diffLine;
	
	public HunkLine(int originalLineNo, int revisedLineNo, DiffLine diffLine) {
		this.originalLineNo = originalLineNo;
		this.revisedLineNo = revisedLineNo;
		this.diffLine = diffLine;
	}

	public int getOriginalLineNo() {
		return originalLineNo;
	}

	public int getRevisedLineNo() {
		return revisedLineNo;
	}

	public DiffLine getDiffLine() {
		return diffLine;
	}

	public void setOriginalLineNo(int originalLineNo) {
		this.originalLineNo = originalLineNo;
	}

	public void setRevisedLineNo(int revisedLineNo) {
		this.revisedLineNo = revisedLineNo;
	}

	public void setDiffLine(DiffLine diffLine) {
		this.diffLine = diffLine;
	}
	
}
