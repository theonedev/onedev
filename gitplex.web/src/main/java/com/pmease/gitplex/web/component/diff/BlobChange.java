package com.pmease.gitplex.web.component.diff;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;

import com.pmease.commons.git.Blob;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.LineProcessor;
import com.pmease.commons.lang.diff.DiffBlock;
import com.pmease.commons.lang.diff.DiffUtils;
import com.pmease.gitplex.core.model.Repository;

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
			newBlobIdent.id = diffEntry.getNewId().toObjectId();
		} else {
			newBlobIdent = new BlobIdent(newCommitHash, null, 0);
		}
		if (changeType != ChangeType.ADD) {
			oldBlobIdent = new BlobIdent(oldCommitHash, diffEntry.getOldPath(), diffEntry.getOldMode().getBits());
			oldBlobIdent.id = diffEntry.getOldId().toObjectId();
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
				Blob newBlob = getRepository().getBlob(newBlobIdent);
				if (newBlob.getText() != null) {
					List<String> newLines = newBlob.getText().getLines(getLineProcessor());
					List<String> oldLines = new ArrayList<>();
					diffs = DiffUtils.diff(oldLines, null, newLines, newBlobIdent.getFileName());
				} else {
					diffs = new ArrayList<>();
				}
			} else if (changeType == ChangeType.DELETE) {
				Blob oldBlob = getRepository().getBlob(oldBlobIdent);
				if (oldBlob.getText() != null) {
					List<String> oldLines = oldBlob.getText().getLines(getLineProcessor());
					List<String> newLines = new ArrayList<>();
					diffs = DiffUtils.diff(oldLines, oldBlobIdent.getFileName(), newLines, null);
				} else {
					diffs = new ArrayList<>();
				}
			} else {
				Blob oldBlob = getRepository().getBlob(oldBlobIdent);
				Blob newBlob = getRepository().getBlob(newBlobIdent);
				if (oldBlob.getText() != null && newBlob.getText() != null) {
					List<String> oldLines = oldBlob.getText().getLines(getLineProcessor());
					List<String> newLines = newBlob.getText().getLines(getLineProcessor());
					diffs = DiffUtils.diff(oldLines, oldBlobIdent.getFileName(), newLines, newBlobIdent.getFileName());
				} else {
					diffs = new ArrayList<>();
				}
			}
		}
		return diffs;
	}

	public void setDiffs(List<DiffBlock> diffs) {
		this.diffs = diffs;
	}
	
	protected abstract Repository getRepository();
	
	protected abstract LineProcessor getLineProcessor();
	
}
