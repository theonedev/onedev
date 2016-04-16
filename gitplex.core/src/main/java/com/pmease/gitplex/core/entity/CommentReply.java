package com.pmease.gitplex.core.entity;

import java.util.Date;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;

@Entity
public class CommentReply extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@Version
	private long version;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private Account user;
	
	@Column(nullable=false)
	private Date date = new Date();
	
	@Lob
	@Column(nullable=false, length=65535)
	private String content;
	
	private String contextCommit;

	public long getVersion() {
		return version;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Comment comment;
	
	@Nullable
	public Account getUser() {
		return user;
	}

	public void setUser(Account user) {
		this.user = user;
	}

	public Date getDate() {
		return date;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void delete() {
		GitPlex.getInstance(Dao.class).remove(this);
	}
	
	public Comment getComment() {
		return comment;
	}

	public void setComment(Comment comment) {
		this.comment = comment;
	}

	/**
	 * @return the contextCommit
	 */
	public String getContextCommit() {
		return contextCommit;
	}

	/**
	 * @param contextCommit the contextCommit to set
	 */
	public void setContextCommit(String contextCommit) {
		this.contextCommit = contextCommit;
	}

}
