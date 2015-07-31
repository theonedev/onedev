package com.pmease.commons.lang.diff;

import java.io.Serializable;
import java.util.List;

import com.pmease.commons.lang.diff.DiffMatchPatch.Operation;
import com.pmease.commons.lang.tokenizers.Token;

public class DiffBlock implements Serializable {

	private static final long serialVersionUID = 1L;

	private final DiffMatchPatch.Operation operation;
	
	private final List<List<Token>> lines;
	
	private final int oldStart, newStart;
	
	public DiffBlock(DiffMatchPatch.Operation operation, List<List<Token>> lines, 
			int oldStart, int newStart) {
		this.operation = operation;
		this.lines = lines;
		this.oldStart = oldStart;
		this.newStart = newStart;
	}

	public DiffMatchPatch.Operation getOperation() {
		return operation;
	}

	public List<List<Token>> getLines() {
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
}
