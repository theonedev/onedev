package com.pmease.gitplex.core.entity;

import java.util.Date;

import javax.annotation.Nullable;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OptimisticLock;

import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.component.TextRange;

/*
 * @DynamicUpdate annotation here along with various @OptimisticLock annotations
 * on certain fields tell Hibernate not to perform version check on those fields
 * which can be updated from background thread.
 */
@Entity
@DynamicUpdate 
public class CodeComment extends AbstractEntity {
	
	private static final long serialVersionUID = 1L;

	public static final int DIFF_CONTEXT_SIZE = 3;

	@Version
	private long version;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Depot depot;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private Account user;

	@Lob
	@Column(nullable=false, length=65535)
	private String content;
	
	@Column(nullable=false)
	private Date date = new Date();
	
	@OptimisticLock(excluded=true)
	@Embedded
    @AttributeOverrides({
        @AttributeOverride(name="revision", column=@Column(name="G_BLOB_REV")),
        @AttributeOverride(name="path", column=@Column(name="G_BLOB_PATH")),
        @AttributeOverride(name="mode", column=@Column(name="G_BLOB_MODE")),
        @AttributeOverride(name="id", column=@Column(name="G_BLOB_ID"))
    })
	private BlobIdent blobIdent;
	
	@OptimisticLock(excluded=true)
	@Embedded
	private TextRange textRange;
	
	@OptimisticLock(excluded=true)
	private String compareCommit;
	
	/**
	 * @return the depot
	 */
	public Depot getDepot() {
		return depot;
	}

	/**
	 * @param depot the depot to set
	 */
	public void setDepot(Depot depot) {
		this.depot = depot;
	}

	@Nullable
	public Account getUser() {
		return user;
	}

	public void setUser(@Nullable Account user) {
		this.user = user;
	}

	public long getVersion() {
		return version;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getDate() {
		return date;
	}

	public void delete() {
		GitPlex.getInstance(Dao.class).remove(this);
	}

	/**
	 * @return the blobIdent
	 */
	public BlobIdent getBlobIdent() {
		return blobIdent;
	}

	/**
	 * @param blobIdent the blobIdent to set
	 */
	public void setBlobIdent(BlobIdent blobIdent) {
		this.blobIdent = blobIdent;
	}

	/**
	 * @return the textRange
	 */
	public TextRange getTextRange() {
		return textRange;
	}

	/**
	 * @param textRange the textRange to set
	 */
	public void setTextRange(TextRange textRange) {
		this.textRange = textRange;
	}

	/**
	 * @return the compareCommit
	 */
	public String getCompareCommit() {
		return compareCommit;
	}

	/**
	 * @param compareCommit the compareCommit to set
	 */
	public void setCompareCommit(String compareCommit) {
		this.compareCommit = compareCommit;
	}

}
