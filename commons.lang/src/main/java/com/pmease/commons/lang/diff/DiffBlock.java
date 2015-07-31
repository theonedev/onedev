package com.pmease.commons.lang.diff;

import java.io.Serializable;
import java.util.List;

import com.pmease.commons.lang.tokenizers.Token;

public class DiffBlock implements Serializable {

	private static final long serialVersionUID = 1L;

	private final DiffMatchPatch.Operation operation;
	
	private final List<List<Token>> lines;
	
	public DiffBlock(DiffMatchPatch.Operation operation, List<List<Token>> lines) {
		this.operation = operation;
		this.lines = lines;
	}

	public DiffMatchPatch.Operation getOperation() {
		return operation;
	}

	public List<List<Token>> getLines() {
		return lines;
	}
	
}
