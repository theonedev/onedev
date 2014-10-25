package com.pmease.gitplex.core.model;

import java.util.Date;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.Comment;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class AbstractPullRequestComment extends AbstractEntity implements Comment {
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private User user;
	
	@Column(nullable=false)
	private String content;
	
	@Column(nullable=false)
	private Date date = new Date();
	
	private boolean resolved;

	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	@Nullable
	@Override
	public User getUser() {
		return user;
	}

	public void setUser(@Nullable User user) {
		this.user = user;
	}

	@Override
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public void saveContent(String content) {
		setContent(content);
		GitPlex.getInstance(Dao.class).persist(this);
	}

	@Override
	public Repository getRepository() {
		return request.getTarget().getRepository();
	}

	@Override
	public void delete() {
		GitPlex.getInstance(Dao.class).remove(this);
	}

	@Override
	public boolean isResolved() {
		return resolved;
	}

	public void setResolved(boolean resolved) {
		this.resolved = resolved;
	}

	@Override
	public void resolve(boolean resolved) {
		setResolved(resolved);
		GitPlex.getInstance(Dao.class).persist(this);
	}

}
