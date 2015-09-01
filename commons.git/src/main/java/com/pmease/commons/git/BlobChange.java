package com.pmease.commons.git;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;

import com.google.common.base.Preconditions;
import com.pmease.commons.lang.diff.DiffBlock;
import com.pmease.commons.lang.diff.DiffMatchPatch.Operation;
import com.pmease.commons.lang.diff.DiffUtils;
import com.pmease.commons.lang.tokenizers.CmToken;

@SuppressWarnings("serial")
public abstract class BlobChange implements Serializable {

	protected final ChangeType type;
	
	protected final BlobIdent oldBlobIdent;
	
	protected final BlobIdent newBlobIdent;

	/*
	 * oldRev and newRev can be used to record branch name so that we can 
	 * switch to branch name for file editing while comparing branches
	 */
	protected final String oldRev;
	
	protected final String newRev;
	
	private transient List<DiffBlock<List<CmToken>>> diffBlocks;
	
	public BlobChange(String oldCommitHash, String newCommitHash, DiffEntry diffEntry, 
			@Nullable String oldRev, @Nullable String newRev) {
		type = diffEntry.getChangeType();
		oldBlobIdent = GitUtils.getOldBlobIdent(diffEntry, oldCommitHash);
		newBlobIdent = GitUtils.getNewBlobIdent(diffEntry, newCommitHash);
		this.oldRev = oldRev;
		this.newRev = newRev;
	}
	
	public ChangeType getType() {
		return type;
	}

	public BlobIdent getOldBlobIdent() {
		return oldBlobIdent;
	}

	public BlobIdent getNewBlobIdent() {
		return newBlobIdent;
	}
	
	public BlobIdent getBlobIdent() {
		return newBlobIdent.path!=null? newBlobIdent: oldBlobIdent;
	}

	public String getPath() {
		return newBlobIdent.path != null? newBlobIdent.path: oldBlobIdent.path;
	}
	
	public List<DiffBlock<List<CmToken>>> getDiffBlocks() {
		if (diffBlocks == null) {
			try {
				if (type == ChangeType.ADD) {
					if (getNewText() != null) {
						List<String> newLines = getNewText().getLines(getLineProcessor());
						if (newLines.size() <= DiffUtils.MAX_DIFF_SIZE) {
							List<String> oldLines = new ArrayList<>();
							diffBlocks = DiffUtils.diff(oldLines, null, newLines, newBlobIdent.getFileName());
						} else {
							diffBlocks = new ArrayList<>();
						}
					} else {
						diffBlocks = new ArrayList<>();
					}
				} else if (type == ChangeType.DELETE) {
					if (getOldText() != null) {
						List<String> oldLines = getOldText().getLines(getLineProcessor());
						if (oldLines.size() <= DiffUtils.MAX_DIFF_SIZE) {
							List<String> newLines = new ArrayList<>();
							diffBlocks = DiffUtils.diff(oldLines, oldBlobIdent.getFileName(), newLines, null);
						} else {
							diffBlocks = new ArrayList<>();
						}
					} else {
						diffBlocks = new ArrayList<>();
					}
				} else if (oldBlobIdent.id != null && oldBlobIdent.id.equals(newBlobIdent.id)) {
					diffBlocks = new ArrayList<>();
				} else {
					if (getOldText() != null && getNewText() != null) {
						List<String> oldLines = getOldText().getLines(getLineProcessor());
						List<String> newLines = getNewText().getLines(getLineProcessor());
						if (oldLines.size() + newLines.size() <= DiffUtils.MAX_DIFF_SIZE)
							diffBlocks = DiffUtils.diff(oldLines, oldBlobIdent.getFileName(), newLines, newBlobIdent.getFileName());
						else 
							diffBlocks = new ArrayList<>();
					} else {
						diffBlocks = new ArrayList<>();
					}
				}
			} catch (Exception e) {
				throw new RuntimeException("Error calculating diff of file: " + getPath(), e);
			}
		}
		return diffBlocks;
	}
	
	public String getOldRev() {
		return oldRev;
	}

	public String getNewRev() {
		return newRev;
	}

	public int getAdditions() {
		int additions = 0;
		for (DiffBlock<List<CmToken>> diff: getDiffBlocks()) {
			if (diff.getOperation() == Operation.INSERT)
				additions += diff.getUnits().size();
		}
		return additions;
	}

	public int getDeletions() {
		int deletions = 0;
		for (DiffBlock<List<CmToken>> diff: getDiffBlocks()) {
			if (diff.getOperation() == Operation.DELETE)
				deletions += diff.getUnits().size();
		}
		return deletions;
	}
	
	public Blob getOldBlob() {
		Preconditions.checkNotNull(oldBlobIdent.path);
		return getBlob(oldBlobIdent);
	}
	
	public Blob getNewBlob() {
		Preconditions.checkNotNull(newBlobIdent.path);
		return getBlob(newBlobIdent);
	}
	
	@Nullable
	public Blob.Text getOldText() {
		return getOldBlob().getText();
	}
	
	@Nullable
	public Blob.Text getNewText() {
		return getNewBlob().getText();
	}
	
	public abstract Blob getBlob(BlobIdent blobIdent);
	
	public abstract LineProcessor getLineProcessor();
	
}
