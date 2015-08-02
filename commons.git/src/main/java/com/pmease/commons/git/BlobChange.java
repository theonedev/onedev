package com.pmease.commons.git;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.AnyObjectId;

import com.pmease.commons.lang.diff.DiffBlock;
import com.pmease.commons.lang.diff.DiffMatchPatch.Operation;
import com.pmease.commons.lang.diff.DiffUtils;

@SuppressWarnings("serial")
public abstract class BlobChange implements Serializable {

	private final ChangeType changeType;
	
	private final BlobIdent oldBlobIdent;
	
	private final BlobIdent newBlobIdent;
	
	private transient List<DiffBlock> diffs;
	
	public BlobChange(String oldCommitHash, String newCommitHash, DiffEntry diffEntry) {
		changeType = diffEntry.getChangeType();
		if (changeType != ChangeType.DELETE) {
			newBlobIdent = new BlobIdent(newCommitHash, diffEntry.getNewPath(), diffEntry.getNewMode().getBits());
			AnyObjectId id = diffEntry.getNewId().toObjectId();
			newBlobIdent.id = id!=null?id.name():null;
		} else {
			newBlobIdent = new BlobIdent(newCommitHash, null, 0);
		}
		if (changeType != ChangeType.ADD) {
			oldBlobIdent = new BlobIdent(oldCommitHash, diffEntry.getOldPath(), diffEntry.getOldMode().getBits());
			AnyObjectId id = diffEntry.getOldId().toObjectId();
			newBlobIdent.id = id!=null?id.name():null;
		} else {
			oldBlobIdent = new BlobIdent(oldCommitHash, null, null);
		}
	}
	
	public ChangeType getChangeType() {
		return changeType;
	}

	public BlobIdent getOldBlobIdent() {
		return oldBlobIdent;
	}

	public BlobIdent getNewBlobIdent() {
		return newBlobIdent;
	}

	public List<DiffBlock> getDiffs() {
		if (diffs == null) {
			if (changeType == ChangeType.ADD) {
				Blob newBlob = getBlob(newBlobIdent);
				if (newBlob.getText() != null) {
					List<String> newLines = newBlob.getText().getLines(getLineProcessor());
					if (newLines.size() <= DiffUtils.MAX_DIFF_LEN) {
						List<String> oldLines = new ArrayList<>();
						diffs = DiffUtils.diff(oldLines, null, newLines, newBlobIdent.getFileName());
					} else {
						diffs = new ArrayList<>();
					}
				} else {
					diffs = new ArrayList<>();
				}
			} else if (changeType == ChangeType.DELETE) {
				Blob oldBlob = getBlob(oldBlobIdent);
				if (oldBlob.getText() != null) {
					List<String> oldLines = oldBlob.getText().getLines(getLineProcessor());
					if (oldLines.size() <= DiffUtils.MAX_DIFF_LEN) {
						List<String> newLines = new ArrayList<>();
						diffs = DiffUtils.diff(oldLines, oldBlobIdent.getFileName(), newLines, null);
					} else {
						diffs = new ArrayList<>();
					}
				} else {
					diffs = new ArrayList<>();
				}
			} else if (oldBlobIdent.id != null && oldBlobIdent.id.equals(newBlobIdent.id)) {
				diffs = new ArrayList<>();
			} else {
				Blob oldBlob = getBlob(oldBlobIdent);
				Blob newBlob = getBlob(newBlobIdent);
				if (oldBlob.getText() != null && newBlob.getText() != null) {
					List<String> oldLines = oldBlob.getText().getLines(getLineProcessor());
					List<String> newLines = newBlob.getText().getLines(getLineProcessor());
					if (oldLines.size() + newLines.size() <= DiffUtils.MAX_DIFF_LEN)
						diffs = DiffUtils.diff(oldLines, oldBlobIdent.getFileName(), newLines, newBlobIdent.getFileName());
					else 
						diffs = new ArrayList<>();
				} else {
					diffs = new ArrayList<>();
				}
			}
		}
		return diffs;
	}
	
	public int getAdditions() {
		int additions = 0;
		for (DiffBlock diff: getDiffs()) {
			if (diff.getOperation() == Operation.INSERT)
				additions += diff.getLines().size();
		}
		return additions;
	}

	public int getDeletions() {
		int deletions = 0;
		for (DiffBlock diff: getDiffs()) {
			if (diff.getOperation() == Operation.DELETE)
				deletions += diff.getLines().size();
		}
		return deletions;
	}
	
	public void setDiffs(List<DiffBlock> diffs) {
		this.diffs = diffs;
	}
	
	public String getPath() {
		return newBlobIdent.path != null? newBlobIdent.path: oldBlobIdent.path;
	}
	
	protected abstract Blob getBlob(BlobIdent blobIdent);
	
	protected abstract LineProcessor getLineProcessor();
	
}
