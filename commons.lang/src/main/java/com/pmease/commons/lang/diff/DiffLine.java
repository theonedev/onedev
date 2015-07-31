package com.pmease.commons.lang.diff;

import java.io.Serializable;
import java.util.List;

import com.pmease.commons.lang.diff.DiffMatchPatch.Operation;
import com.pmease.commons.lang.tokenizers.CmToken;

public class DiffLine implements Serializable {

	private static final long serialVersionUID = 1L;

	private final DiffMatchPatch.Operation operation;
	
	private final List<CmToken> tokens;
	
	private final int oldLineNo, newLineNo;
	
	public DiffLine(DiffMatchPatch.Operation operation, List<CmToken> tokens, 
			int oldLineNo, int newLineNo) {
		this.operation = operation;
		this.tokens = tokens;
		this.oldLineNo = oldLineNo;
		this.newLineNo = newLineNo;
	}

	public DiffMatchPatch.Operation getOperation() {
		return operation;
	}

	public List<CmToken> getTokens() {
		return tokens;
	}

	public int getOldLineNo() {
		return oldLineNo;
	}

	public int getNewLineNo() {
		return newLineNo;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		if (operation == Operation.INSERT)
			buffer.append("+");
		else if (operation == Operation.DELETE)
			buffer.append("-");
		else
			buffer.append(" ");
		for (CmToken token: tokens)
			buffer.append(token);
		return buffer.toString();
	}
	
}
