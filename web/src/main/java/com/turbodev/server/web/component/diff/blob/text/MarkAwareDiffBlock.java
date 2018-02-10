package com.turbodev.server.web.component.diff.blob.text;

import java.io.Serializable;
import java.util.List;

import com.turbodev.jsyntax.Tokenized;

public class MarkAwareDiffBlock implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Type {
		DELETE, INSERT, MARKED_EQUAL, EQUAL
	}

	private final Type type;
	
	private final List<Tokenized> lines;
	
	private final int oldStart, newStart;
	
	public MarkAwareDiffBlock(Type type, List<Tokenized> lines, int oldStart, int newStart) {
		this.type = type;
		this.lines = lines;
		this.oldStart = oldStart;
		this.newStart = newStart;
	}

	public Type getType() {
		return type;
	}

	public List<Tokenized> getLines() {
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
