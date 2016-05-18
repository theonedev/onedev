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
import com.pmease.commons.lang.diff.WhitespaceOption;
import com.pmease.commons.lang.tokenizers.CmToken;

@SuppressWarnings("serial")
public abstract class BlobChange implements Serializable {

	protected final ChangeType type;
	
	protected final WhitespaceOption whitespaceOption;
	
	protected final BlobIdent oldBlobIdent;
	
	protected final BlobIdent newBlobIdent;
	
	private transient List<DiffBlock<List<CmToken>>> diffBlocks;
	
	public BlobChange(String oldRev, String newRev, DiffEntry diffEntry, 
			WhitespaceOption whitespaceOption) {
		if (diffEntry.getChangeType() == ChangeType.RENAME 
				&& diffEntry.getOldPath().equals(diffEntry.getNewPath())) {
			// for some unknown reason, jgit detects rename even if path 
			// is the same
			type = ChangeType.MODIFY;
		} else {
			type = diffEntry.getChangeType();
		}
		this.whitespaceOption = whitespaceOption;
		oldBlobIdent = GitUtils.getOldBlobIdent(diffEntry, oldRev);
		newBlobIdent = GitUtils.getNewBlobIdent(diffEntry, newRev);
	}
	
	public BlobChange(ChangeType type, BlobIdent oldBlobIdent, BlobIdent newBlobIdent, 
			WhitespaceOption whitespaceOption) {
		this.type = type;
		this.oldBlobIdent = oldBlobIdent;
		this.newBlobIdent = newBlobIdent;
		this.whitespaceOption = whitespaceOption;
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
				if (type == ChangeType.ADD || type == ChangeType.COPY) {
					if (getNewText() != null) {
						List<String> newLines = getNewText().getLines();
						if (newLines.size() <= DiffUtils.MAX_DIFF_SIZE) {
							List<String> oldLines = new ArrayList<>();
							diffBlocks = DiffUtils.diff(
									oldLines, null, 
									newLines, newBlobIdent.isFile()?newBlobIdent.path:null, 
									WhitespaceOption.DEFAULT);
						} else {
							diffBlocks = new ArrayList<>();
						}
					} else {
						diffBlocks = new ArrayList<>();
					}
				} else if (type == ChangeType.DELETE) {
					if (getOldText() != null) {
						List<String> oldLines = getOldText().getLines();
						if (oldLines.size() <= DiffUtils.MAX_DIFF_SIZE) {
							List<String> newLines = new ArrayList<>();
							diffBlocks = DiffUtils.diff(
									oldLines, oldBlobIdent.isFile()?newBlobIdent.path:null, 
									newLines, null, 
									WhitespaceOption.DEFAULT);
						} else {
							diffBlocks = new ArrayList<>();
						}
					} else {
						diffBlocks = new ArrayList<>();
					}
				} else {
					if (getOldText() != null && getNewText() != null) {
						List<String> oldLines = getOldText().getLines();
						List<String> newLines = getNewText().getLines();
						if (oldLines.size() + newLines.size() <= DiffUtils.MAX_DIFF_SIZE) {
							diffBlocks = DiffUtils.diff(
									oldLines, oldBlobIdent.isFile()?newBlobIdent.path:null, 
									newLines, newBlobIdent.isFile()?newBlobIdent.path:null, 
									whitespaceOption);
						} else { 
							diffBlocks = new ArrayList<>();
						}
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
	
}
