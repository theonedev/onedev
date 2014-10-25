package com.pmease.commons.util.diff;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class DiffLine implements Serializable {
	
	public enum Action {
		
		ADD, 
		EQUAL, 
		DELETE;
		
		public static Action fromOperation(DiffMatchPatch.Operation operation) {
			if (operation == DiffMatchPatch.Operation.DELETE)
				return DELETE;
			else if (operation == DiffMatchPatch.Operation.EQUAL)
				return EQUAL;
			else
				return ADD;
		}
		
		public DiffMatchPatch.Operation toOperation() {
			if (this == ADD)
				return DiffMatchPatch.Operation.INSERT;
			else if (this == EQUAL)
				return DiffMatchPatch.Operation.EQUAL;
			else
				return DiffMatchPatch.Operation.DELETE;
		}
	}
	
	private final Action action;
	
	private final int oldLineNo;
	
	private final int newLineNo;
	
	private final List<Token> partials;
	
	public DiffLine(Action action, String line, int oldLineNo, int newLineNo) {
		this.action = action;
		this.oldLineNo = oldLineNo;
		this.newLineNo = newLineNo;
		this.partials = Lists.newArrayList(new Token(line, false));
	}
	
	public DiffLine(Action action, List<Token> partials, int oldLineNo, int newLineNo) {
		this.action = action;
		this.partials = partials;
		this.oldLineNo = oldLineNo;
		this.newLineNo = newLineNo;
	}
	
	public Action getAction() {
		return action;
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
		return partials;
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
		for (Token word: partials) 
			buffer.append(word.toString());
		
		if (action == Action.DELETE)
			return "-" + buffer.toString();
		else if (action == Action.EQUAL)
			return " " + buffer.toString();
		else
			return "+" + buffer.toString();
	}
	
}
