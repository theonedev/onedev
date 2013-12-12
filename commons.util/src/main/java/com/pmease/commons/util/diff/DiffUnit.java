package com.pmease.commons.util.diff;

import javax.annotation.Nullable;

public class DiffUnit {
	
	public enum Action {
		INSERT, 
		EQUAL, 
		DELETE;
		
		public static Action fromOperation(DiffMatchPatch.Operation operation) {
			if (operation == DiffMatchPatch.Operation.DELETE)
				return DELETE;
			else if (operation == DiffMatchPatch.Operation.EQUAL)
				return EQUAL;
			else
				return INSERT;
		}
		
		public DiffMatchPatch.Operation toOperation() {
			if (this == INSERT)
				return DiffMatchPatch.Operation.INSERT;
			else if (this == EQUAL)
				return DiffMatchPatch.Operation.EQUAL;
			else
				return DiffMatchPatch.Operation.DELETE;
		}
	}
	
	private final Action action;
	
	private final String text;
	
	private final String warnings;
	
	public DiffUnit(Action action, String text, @Nullable String warnings) {
		this.action = action;
		this.text = text;
		this.warnings = warnings;
	}
	
	public DiffUnit(Action action, String text) {
		this(action, text, null);
	}

	public Action getAction() {
		return action;
	}
	
	public String getText() {
		return text;
	}
	
	/**
	 * Get warnings attached to this diff unit. 
	 * 
	 * @return
	 * 			warnings attached to this diff unit. Multiple warnings are separated 
	 * 			by new line character. <tt>null</tt> will be returned if no warnings. 
	 */
	public @Nullable String getWarnings() {
		return warnings;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		if (action == Action.DELETE)
			buffer.append("-").append(text);
		else if (action == Action.EQUAL)
			buffer.append(" ").append(text);
		else
			buffer.append("+").append(text);
		
		if (warnings != null)
			buffer.append("\n").append(warnings);

		return buffer.toString();
	}	
}
