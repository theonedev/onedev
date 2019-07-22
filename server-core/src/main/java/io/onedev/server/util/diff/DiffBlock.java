package io.onedev.server.util.diff;

import java.io.Serializable;
import java.util.List;

import io.onedev.server.util.diff.DiffMatchPatch.Operation;

public class DiffBlock<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private final DiffMatchPatch.Operation operation;
	
	private final List<T> units;
	
	private final int oldStart, newStart;
	
	public DiffBlock(DiffMatchPatch.Operation operation, List<T> units, 
			int oldStart, int newStart) {
		this.operation = operation;
		this.units = units;
		this.oldStart = oldStart;
		this.newStart = newStart;
	}

	public DiffMatchPatch.Operation getOperation() {
		return operation;
	}

	public List<T> getUnits() {
		return units;
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
			return oldStart + units.size();
	}
	
	public int getNewEnd() {
		if (operation == Operation.DELETE)
			return newStart;
		else
			return newStart + units.size();
	}
	
}
