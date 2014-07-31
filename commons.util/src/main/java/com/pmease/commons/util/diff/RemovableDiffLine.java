package com.pmease.commons.util.diff;

class RemovableDiffLine {
	
	DiffLine diffLine;
	
	boolean removed;
	
	RemovableDiffLine(DiffLine diffLine, boolean removed) {
		this.diffLine = diffLine;
		this.removed = removed;
	}
}