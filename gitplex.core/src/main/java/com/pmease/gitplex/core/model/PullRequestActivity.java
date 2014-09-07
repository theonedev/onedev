package com.pmease.gitplex.core.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

@Entity
public class PullRequestActivity {

	@ManyToOne
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@Lob
	@Column(nullable=false)
	private PullRequestAction action;
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private User user;
	
	@Column(nullable=false)
	private Date date;

	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	public PullRequestAction getAction() {
		return action;
	}

	public void setAction(PullRequestAction action) {
		this.action = action;
	}

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
	
}
