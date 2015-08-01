package com.pmease.commons.git;

import java.util.List;

import com.pmease.commons.lang.diff.DiffBlock;

public class BlobDiff extends BlobChange {

	private static final long serialVersionUID = 1L;

	private final List<DiffBlock> diffs;
	
	public BlobDiff(BlobIdent oldBlobIdent, BlobIdent newBlobIdent, List<DiffBlock> diffs) {
		super(oldBlobIdent, newBlobIdent);
		
		this.diffs = diffs;
	}

	public List<DiffBlock> getDiffs() {
		return diffs;
	}

}
