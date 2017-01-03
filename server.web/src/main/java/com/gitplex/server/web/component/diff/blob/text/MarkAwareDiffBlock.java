package com.gitplex.server.web.component.diff.blob.text;

import java.io.Serializable;
import java.util.List;

import com.gitplex.jsyntax.Token;

public class MarkAwareDiffBlock implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Type {
		DELETE, INSERT, MARKED_EQUAL, EQUAL
	}

	private final Type type;
	
	private final List<List<Token>> lines;
	
	private final int oldStart, newStart;
	
	public MarkAwareDiffBlock(Type type, List<List<Token>> lines, int oldStart, int newStart) {
		this.type = type;
		this.lines = lines;
		this.oldStart = oldStart;
		this.newStart = newStart;
	}

	public Type getType() {
		return type;
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
		if (type == Type.INSERT)
			return oldStart;
		else
			return oldStart + lines.size();
	}
	
	public int getNewEnd() {
		if (type == Type.DELETE)
			return newStart;
		else
			return newStart + lines.size();
	}
	
}
