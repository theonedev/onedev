package com.pmease.commons.lang.diff;

import java.io.Serializable;
import java.util.List;

import com.pmease.commons.lang.diff.DiffMatchPatch.Operation;
import com.pmease.commons.lang.tokenizers.Token;

@SuppressWarnings("serial")
public class DiffLine implements Serializable {
	
	private final Operation operation;
	
	private final int oldLineNo;
	
	private final int newLineNo;
	
	private final List<Token> tokens;
	
	public DiffLine(Operation operation, List<Token> tokens, int oldLineNo, int newLineNo) {
		this.operation = operation;
		this.tokens = tokens;
		this.oldLineNo = oldLineNo;
		this.newLineNo = newLineNo;
	}
	
	public Operation getOperation() {
		return operation;
	}
	
	/**
	 * Get list of partials of this diff line. Concatenate all partial contents will 
	 * form a line, and partials with {@link Token#isEmphasized()} indicates 
	 * whether or not a partial should be emphasized when displayed as they cause 
	 * the line to be considered as a diff line.
	 * 
	 * @return
	 * 			list of partials of this diff line
	 */
	public List<Token> getTokens() {
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
		for (Token token: tokens) 
			buffer.append(token.toString());
		
		if (operation == Operation.DELETE)
			return "-" + buffer.toString();
		else if (operation == Operation.EQUAL)
			return " " + buffer.toString();
		else
			return "+" + buffer.toString();
	}
	
}
