package com.pmease.gitplex.core.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.pmease.commons.hibernate.AbstractEntity;

@SuppressWarnings("serial")
@Entity
public class Comment extends AbstractEntity {
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private PullRequestUpdate update;
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private User user;
	
	@Column(nullable=false)
	private Date date = new Date();

	@Column(nullable=false)
	private String content;
	
	@Embedded
	private CommentPosition position;
	
	public PullRequestUpdate getUpdate() {
		return update;
	}

	public void setUpdate(PullRequestUpdate update) {
		this.update = update;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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

	public void setDate(Date date) {
		this.date = date;
	}

	public CommentPosition getPosition() {
		return position;
	}

	public void setPosition(CommentPosition position) {
		this.position = position;
	}
	
}
