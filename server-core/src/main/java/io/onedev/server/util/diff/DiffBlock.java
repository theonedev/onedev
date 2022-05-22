package io.onedev.server.util.diff;

import java.io.Serializable;
import java.util.List;

import io.onedev.server.util.diff.DiffMatchPatch.Operation;

public class DiffBlock<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private final DiffMatchPatch.Operation operation;
	
	private final List<T> elements;
	
	private final int oldStart, newStart;
	
	public DiffBlock(DiffMatchPatch.Operation operation, List<T> elements, 
			int oldStart, int newStart) {
		this.operation = operation;
		this.elements = elements;
		this.oldStart = oldStart;
		this.newStart = newStart;
	}

	public DiffMatchPatch.Operation getOperation() {
		return operation;
	}

	public List<T> getElements() {
		return elements;
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
			return oldStart + elements.size();
	}
	
	public int getNewEnd() {
		if (operation == Operation.DELETE)
			return newStart;
		else
			return newStart + elements.size();
	}
	
}
