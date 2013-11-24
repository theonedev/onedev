package com.pmease.commons.git;

import java.util.ArrayList;
import java.util.List;

import com.pmease.commons.util.diff.DiffChunk;

@SuppressWarnings("serial")
public class FileChangeWithDiffs extends FileChange {

	private final boolean binary;
	
	private final String commitHash1;
	
	private final String commitHash2;
	
	private final List<DiffChunk> diffChunks;
	
	public FileChangeWithDiffs(Action action, String path, boolean binary, 
			String commitHash1, String commitHash2, List<DiffChunk> diffChunks) {
		super(action, path);
		
		this.binary = binary;
		this.commitHash1 = commitHash1;
		this.commitHash2 = commitHash2;
		this.diffChunks = new ArrayList<>(diffChunks);
	}

	public boolean isBinary() {
		return binary;
	}

	public List<DiffChunk> getDiffChunks() {
		return diffChunks;
	}

	public String getCommitHash1() {
		return commitHash1;
	}

	public String getCommitHash2() {
		return commitHash2;
	}
 
}
