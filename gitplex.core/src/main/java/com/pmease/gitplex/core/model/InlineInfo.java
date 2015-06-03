package com.pmease.gitplex.core.model;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Lob;

import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.util.diff.AroundContext;

@Embeddable
public class InlineInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	@Embedded
    @AttributeOverrides({
        @AttributeOverride(name="revision", column=@Column(name="G_BLOB_REV")),
        @AttributeOverride(name="path", column=@Column(name="G_BLOB_PATH")),
        @AttributeOverride(name="mode", column=@Column(name="G_BLOB_MODE"))
    })
 	private BlobIdent blobInfo;

	@Embedded
    @AttributeOverrides({
        @AttributeOverride(name="revision", column=@Column(name="G_COMPARE_REV") ),
        @AttributeOverride(name="path", column=@Column(name="G_COMPARE_PATH")),
        @AttributeOverride(name="mode", column=@Column(name="G_COMPARE_MODE"))
    })
	private BlobIdent compareWith;
	
	// Use Integer instead of int here in order to allow this class to be 
	// embedded as nullable property of JPA entities 
	private Integer line;

	@Lob
	@Column(length=65535)
	private AroundContext context;

	public BlobIdent getBlobInfo() {
		return blobInfo;
	}

	public void setBlobInfo(BlobIdent blobInfo) {
		this.blobInfo = blobInfo;
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

	public AroundContext getContext() {
		return context;
	}

	public void setContext(AroundContext context) {
		this.context = context;
	}

}
