package com.pmease.commons.lang.diff;

import java.io.Serializable;
import java.util.List;

import com.pmease.commons.lang.diff.DiffMatchPatch.Operation;

@SuppressWarnings("serial")
public class DiffHunk implements Serializable {

	private int oldStart, oldEnd, newStart, newEnd;
	
	private final List<DiffLine> diffLines;

	public DiffHunk(int oldStart, int newStart, List<DiffLine> diffLines) {
		this.oldStart = oldStart;
		this.newStart = newStart;
		this.diffLines = diffLines;
		
		oldEnd = oldStart; 
		newEnd = newStart;
		for (DiffLine diffLine: diffLines) {
			if (diffLine.getOperation() == Operation.INSERT) {
				newEnd++;
			} else if (diffLine.getOperation() == Operation.DELETE) {
				oldEnd++;
			} else { 
				oldEnd++;
				newEnd++;
			}
		}
	}

	/**
	 * Get starting row number of original part of this diff chunk. <tt>0</tt> represents
	 * the first row. 
	 *   
	 * @return
	 * 			starting row number of original part of the diff chunk
	 */
	public int getOldStart() {
		return oldStart;
	}
	
	/**
	 * Get starting row number of revised part of this diff chunk. <tt>0</tt> represents
	 * the first row. 
	 *   
	 * @return
	 * 			starting row number of revised part of the diff chunk
	 */
	public int getNewStart() {
		return newStart;
	}
	
	public int getOldEnd() {
		return oldEnd;
	}

	public void setOldEnd(int oldEnd) {
		this.oldEnd = oldEnd;
	}

	public int getNewEnd() {
		return newEnd;
	}

	public void setNewEnd(int newEnd) {
		this.newEnd = newEnd;
	}

	public void setOldStart(int oldStart) {
		this.oldStart = oldStart;
	}

	public void setNewStart(int newStart) {
		this.newStart = newStart;
	}

	public List<DiffLine> getDiffLines() {
		return diffLines;
	}
	
	/**
	 * Get string representation of this diff chunk. It will mimics the unified diff format, 
	 * so row number will be increased by one, that is, the first row will be represented 
	 * by <tt>1</tt>.
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer(describe(oldStart, newStart, oldEnd, newEnd));
		buffer.append("\n");
		for (DiffLine line: diffLines)
			buffer.append(line).append("\n");
		return buffer.toString();
	}
	
	public static String describe(int oldStart, int newStart, int oldEnd, int newEnd) {
		StringBuffer buffer = new StringBuffer();
		int oldCount = oldEnd-oldStart;
		int newCount = newEnd-newStart;
		buffer.append("@@ -").append(oldCount!=0?oldStart+1:0).append(",").append(oldCount)
				.append(" +").append(newCount!=0?newStart+1:0).append(",").append(newCount).append(" @@");
		return buffer.toString();
	}
}
