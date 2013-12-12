package com.pmease.commons.util.diff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiffChunk {

	private final int start1;
	
	private final int start2;
	
	public final List<DiffUnit> diffUnits;

	public DiffChunk(int start1, int start2, List<DiffUnit> diffUnits) {
		this.start1 = start1;
		this.start2 = start2;
		this.diffUnits = new ArrayList<>(diffUnits);
	}

	/**
	 * Get starting row number of original part of this diff chunk. <tt>0</tt> represents
	 * the first row. 
	 *   
	 * @return
	 * 			starting row number of original part of the diff chunk
	 */
	public int getStart1() {
		return start1;
	}
	
	/**
	 * Get starting row number of revised part of this diff chunk. <tt>0</tt> represents
	 * the first row. 
	 *   
	 * @return
	 * 			starting row number of revised part of the diff chunk
	 */
	public int getStart2() {
		return start2;
	}
	
	public List<DiffUnit> getDiffUnits() {
		return Collections.unmodifiableList(diffUnits);
	}
	
	/**
	 * Get string representation of this diff chunk. It will mimics the unified diff format, 
	 * so row number will be increased by one, that is, the first row will be represented 
	 * by <tt>1</tt>.
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("@@ -").append(start1+1).append(" +").append(start2+1).append(" @@\n");
		for (DiffUnit token: diffUnits)
			buffer.append(token).append("\n");
		return buffer.toString();
	}
	
}
