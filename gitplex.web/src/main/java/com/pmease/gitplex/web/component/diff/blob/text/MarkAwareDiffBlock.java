package com.pmease.gitplex.web.component.diff.blob.text;

import java.io.Serializable;
import java.util.List;

import com.pmease.commons.lang.tokenizers.CmToken;

public class MarkAwareDiffBlock implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Type {
		DELETE, INSERT, MARKED_EQUAL, EQUAL
	}

	private final Type type;
	
	private final List<List<CmToken>> lines;
	
	private final int oldStart, newStart;
	
	public MarkAwareDiffBlock(Type type, List<List<CmToken>> lines, int oldStart, int newStart) {
		this.type = type;
		this.lines = lines;
		this.oldStart = oldStart;
		this.newStart = newStart;
	}

	public Type getType() {
		return type;
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
