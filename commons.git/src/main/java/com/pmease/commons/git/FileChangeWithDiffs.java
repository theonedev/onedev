package com.pmease.commons.git;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.FileMode;

import com.pmease.commons.util.diff.DiffChunk;

@SuppressWarnings("serial")
public class FileChangeWithDiffs extends FileChange {

	private final boolean binary;
	
	private final FileMode mode;
	
	private final String oldCommit;
	
	private final String newCommit;
	
	private final List<DiffChunk> diffChunks;
	
	public FileChangeWithDiffs(Action action, String oldPath, String newPath, 
			FileMode mode, boolean binary, @Nullable String oldCommit, 
			@Nullable String newCommit, List<DiffChunk> diffChunks) {
		super(action, oldPath, newPath);
		
		this.mode = mode;
		this.binary = binary;
		this.oldCommit = oldCommit;
		this.newCommit = newCommit;
		this.diffChunks = new ArrayList<>(diffChunks);
	}

	public FileMode getMode() {
		return mode;
	}

	public boolean isBinary() {
		return binary;
	}

	public List<DiffChunk> getDiffChunks() {
		return diffChunks;
	}

	public @Nullable String getOldCommit() {
		return oldCommit;
	}

	public @Nullable String getNewCommit() {
		return newCommit;
	}
 
}
