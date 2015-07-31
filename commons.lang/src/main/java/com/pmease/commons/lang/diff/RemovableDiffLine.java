package com.pmease.commons.lang.diff;

class RemovableDiffLine {
	
	DiffLine diffLine;
	
	boolean removed;
	
	RemovableDiffLine(DiffLine diffLine, boolean removed) {
		this.diffLine = diffLine;
		this.removed = removed;
	}
}