package com.pmease.gitplex.core.model;

import java.util.Date;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import com.pmease.commons.hibernate.AbstractEntity;

@SuppressWarnings("serial")
@Entity
public class PullRequestAudit extends AbstractEntity {

	@ManyToOne
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@Lob
	@Column(nullable=false)
	private PullRequestAction action;
	
	@ManyToOne
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

	@Nullable
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
