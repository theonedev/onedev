package com.pmease.gitplex.core.entity;

import java.util.Date;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OptimisticLock;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.component.CommentPosition;

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
	private CommentPosition position;
	
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

	public CommentPosition getPosition() {
		return position;
	}

	public void setPosition(CommentPosition position) {
		this.position = position;
	}

	public BlobIdent getBlobIdent() {
		return Preconditions.checkNotNull(position).getBlobIdent();
	}
	
	public void setBlobIdent(BlobIdent blobIdent) {
		if (position == null)
			position = new CommentPosition();
		position.setBlobIdent(blobIdent);
	}
	
	public BlobIdent getCompareWith() {
		return Preconditions.checkNotNull(position).getCompareWith();
	}
	
	public void setCompareWith(BlobIdent compareWith) {
		if (position == null)
			position = new CommentPosition();
		position.setCompareWith(compareWith);
	}

	public int getBeginLine() {
		return Preconditions.checkNotNull(position).getBeginLine();
	}

	public void setBeginLine(int beginLine) {
		if (position == null)
			position = new CommentPosition();
		position.setBeginLine(beginLine);
	}
	
	public int getEndLine() {
		return Preconditions.checkNotNull(position).getEndLine();
	}

	public void setEndLine(int endLine) {
		if (position == null)
			position = new CommentPosition();
		position.setEndLine(endLine);
	}
	
}
