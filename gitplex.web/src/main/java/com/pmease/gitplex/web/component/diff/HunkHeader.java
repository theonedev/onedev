package com.pmease.gitplex.web.component.diff;

import java.io.Serializable;

@SuppressWarnings("serial")
class HunkHeader implements Serializable {
	
	private int oldStart, oldEnd, newStart, newEnd;
	
	public HunkHeader(int oldStart, int oldEnd, int newStart, int newEnd) {
		this.oldStart = oldStart;
		this.oldEnd = oldEnd;
		this.newStart = newStart;
		this.newEnd = newEnd;
	}

	public int getOldStart() {
		return oldStart;
	}

	public int getOldEnd() {
		return oldEnd;
	}

	public int getNewStart() {
		return newStart;
	}

	public int getNewEnd() {
		return newEnd;
	}

	public void setOldStart(int oldStart) {
		this.oldStart = oldStart;
	}

	public void setOldEnd(int oldEnd) {
		this.oldEnd = oldEnd;
	}

	public void setNewStart(int newStart) {
		this.newStart = newStart;
	}

	public void setNewEnd(int newEnd) {
		this.newEnd = newEnd;
	}

	@Override
	public String toString() {
		int oldStart = this.oldStart;
		int newStart = this.newStart;
		int oldCount = oldEnd - oldStart;
		int newCount = newEnd - newStart;

		if (oldCount == 0)
			oldStart = 0;
		else
			oldStart++;
		if (newCount == 0)
			newStart = 0;
		else
			newStart++;
		return "@@ -" + oldStart + "," + oldCount + " +" + newStart + "," + newCount + " @@";
	}
}
