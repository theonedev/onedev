package com.pmease.commons.git;

import java.util.ArrayList;
import java.util.List;

import com.pmease.commons.util.diff.DiffChunk;

@SuppressWarnings("serial")
public class FileChangeWithDiffs extends FileChange {

	private final boolean binary;
	
	private final List<DiffChunk> diffChunks;
	
	public FileChangeWithDiffs(String path, Action action, boolean binary, List<DiffChunk> diffChunks) {
		super(path, action);
		
		this.binary = binary;
		this.diffChunks = new ArrayList<>(diffChunks);
	}

	public boolean isBinary() {
		return binary;
	}

	public List<DiffChunk> getDiffChunks() {
		return diffChunks;
	}
 
}
