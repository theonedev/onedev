package com.pmease.commons.lang.diff;

import java.io.Serializable;
import java.util.List;

import com.pmease.commons.lang.diff.DiffMatchPatch.Operation;

public class SimpleDiffBlock implements Serializable {

	private static final long serialVersionUID = 1L;

	private final DiffMatchPatch.Operation operation;
	
	private final List<String> lines;
	
	private final int oldStart, newStart;
	
	public SimpleDiffBlock(DiffMatchPatch.Operation operation, List<String> lines, 
			int oldStart, int newStart) {
		this.operation = operation;
		this.lines = lines;
		this.oldStart = oldStart;
		this.newStart = newStart;
	}

	public DiffMatchPatch.Operation getOperation() {
		return operation;
	}

	public List<String> getLines() {
		return lines;
	}

	public int getOldStart() {
		return oldStart;
	}

	public int getNewStart() {
		return newStart;
	}
	
	public int getOldEnd() {
		if (operation == Operation.INSERT)
			return oldStart;
		else
			return oldStart + lines.size();
	}
	
	public int getNewEnd() {
		if (operation == Operation.DELETE)
			return newStart;
		else
			return newStart + lines.size();
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (String line: lines) {
			if (operation == Operation.INSERT)
				buffer.append("+");
			else if (operation == Operation.DELETE)
				buffer.append("-");
			else
				buffer.append(" ");
			buffer.append(line).append("\n");
		}
		return buffer.toString();
	}
	
}
