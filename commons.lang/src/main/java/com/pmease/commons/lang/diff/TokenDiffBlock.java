package com.pmease.commons.lang.diff;

import java.io.Serializable;
import java.util.List;

import com.pmease.commons.lang.diff.DiffMatchPatch.Operation;
import com.pmease.commons.lang.tokenizers.CmToken;

public class TokenDiffBlock implements Serializable {

	private static final long serialVersionUID = 1L;

	private final DiffMatchPatch.Operation operation;
	
	private final List<CmToken> tokens;
	
	private final int oldStart, newStart;
	
	public TokenDiffBlock(DiffMatchPatch.Operation operation, List<CmToken> tokens, 
			int oldStart, int newStart) {
		this.operation = operation;
		this.tokens = tokens;
		this.oldStart = oldStart;
		this.newStart = newStart;
	}

	public DiffMatchPatch.Operation getOperation() {
		return operation;
	}

	public List<CmToken> getTokens() {
		return tokens;
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
			return oldStart + tokens.size();
	}
	
	public int getNewEnd() {
		if (operation == Operation.DELETE)
			return newStart;
		else
			return newStart + tokens.size();
	}
	
}
