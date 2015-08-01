package com.pmease.commons.git;

import java.io.Serializable;

@SuppressWarnings("serial")
public class BlobChange implements Serializable {

	private final BlobIdent oldBlobIdent;
	
	private final BlobIdent newBlobIdent;
	
	public BlobChange(BlobIdent oldBlobIdent, BlobIdent newBlobIdent) {
		this.oldBlobIdent = oldBlobIdent;
		this.newBlobIdent = newBlobIdent;
	}
	
	public BlobIdent getOldBlobIdent() {
		return oldBlobIdent;
	}

	public BlobIdent getNewBlobIdent() {
		return newBlobIdent;
	}
	
}
