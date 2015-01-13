package com.pmease.gitplex.core.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.CommentReply;

@SuppressWarnings("serial")
@Entity
public class PullRequestCommentReply extends AbstractEntity implements CommentReply {

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;
	
	@Column(nullable=false)
	private Date date;
	
	@Column(nullable=false, length=65535)
	private String content;

	@ManyToOne
	@JoinColumn(nullable=false)
	private PullRequestComment comment;
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public void saveContent(String content) {
		setContent(content);
		GitPlex.getInstance(Dao.class).persist(this);
	}

	@Override
	public void delete() {
		GitPlex.getInstance(Dao.class).remove(this);
	}
	
	@Override
	public PullRequestComment getComment() {
		return comment;
	}

	public void setComment(PullRequestComment comment) {
		this.comment = comment;
	}

}
