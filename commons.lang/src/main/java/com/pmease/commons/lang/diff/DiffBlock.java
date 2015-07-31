package com.pmease.commons.lang.diff;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.pmease.commons.lang.diff.DiffMatchPatch.Operation;
import com.pmease.commons.lang.tokenizers.CmToken;

public class DiffBlock implements Serializable {

	private static final long serialVersionUID = 1L;

	private final DiffMatchPatch.Operation operation;
	
	private final List<List<CmToken>> lines;
	
	private final int oldStart, newStart;
	
	public DiffBlock(DiffMatchPatch.Operation operation, List<List<CmToken>> lines, 
			int oldStart, int newStart) {
		this.operation = operation;
		this.lines = lines;
		this.oldStart = oldStart;
		this.newStart = newStart;
	}

	public DiffMatchPatch.Operation getOperation() {
		return operation;
	}

	public List<List<CmToken>> getLines() {
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
	
	public List<DiffLine> toDiffLines() {
		List<DiffLine> diffLines = new ArrayList<>();
		int oldLineNo = oldStart;
		int newLineNo = newStart;
		for (List<CmToken> line: lines) {
			diffLines.add(new DiffLine(operation, line, oldLineNo, newLineNo));
			if (operation == Operation.EQUAL) {
				oldLineNo++;
				newLineNo++;
			} else if (operation == Operation.DELETE) {
				oldLineNo++;
			} else {
				newLineNo++;
			}
		}
		return diffLines;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (List<CmToken> line: lines) {
			if (operation == Operation.INSERT)
				buffer.append("+");
			else if (operation == Operation.DELETE)
				buffer.append("-");
			else
				buffer.append(" ");
			for (CmToken token: line)
				buffer.append(token);
			buffer.append("\n");
		}
		return buffer.toString();
	}
	
}
