package com.pmease.gitplex.core.model;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import com.pmease.commons.git.BlobIdent;

@Embeddable
public class InlineInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	@Embedded
    @AttributeOverrides({
        @AttributeOverride(name="revision", column=@Column(name="G_BLOB_REV")),
        @AttributeOverride(name="path", column=@Column(name="G_BLOB_PATH")),
        @AttributeOverride(name="mode", column=@Column(name="G_BLOB_MODE")),
        @AttributeOverride(name="id", column=@Column(name="G_BLOB_ID"))
    })
 	private BlobIdent blobIdent;

	@Embedded
    @AttributeOverrides({
        @AttributeOverride(name="revision", column=@Column(name="G_COMPARE_REV") ),
        @AttributeOverride(name="path", column=@Column(name="G_COMPARE_PATH")),
        @AttributeOverride(name="mode", column=@Column(name="G_COMPARE_MODE")),
        @AttributeOverride(name="id", column=@Column(name="G_COMPARE_ID"))
    })
	private BlobIdent compareWith;
	
	// Use Integer instead of int here in order to allow this class to be 
	// embedded as nullable property of JPA entities 
	private Integer line;

	public BlobIdent getBlobIdent() {
		return blobIdent;
	}

	public void setBlobIdent(BlobIdent blobIdent) {
		this.blobIdent = blobIdent;
	}

	public BlobIdent getCompareWith() {
		return compareWith;
	}

	public void setCompareWith(BlobIdent compareWith) {
		this.compareWith = compareWith;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

}
