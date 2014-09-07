package com.pmease.gitplex.core.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@SuppressWarnings("serial")
@Entity
public class InlineCommentReply implements Serializable {

	@ManyToOne
	@JoinColumn(nullable=false)
	private InlineComment comment;

	@ManyToOne
	@JoinColumn(nullable=false)
	private User user;

	@Column(nullable=false)
	private Date date;
	
	@Column(nullable=false)
	private String content;

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
	
}
