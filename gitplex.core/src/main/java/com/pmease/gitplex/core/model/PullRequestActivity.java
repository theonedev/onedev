package com.pmease.gitplex.core.model;

import java.util.Date;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.pmease.commons.hibernate.AbstractEntity;

@SuppressWarnings("serial")
@Entity
public class PullRequestActivity extends AbstractEntity {

	public enum Action {OPEN, INTEGRATE, DISCARD, APPROVE, DISAPPROVE, UNDO_REVIEW, 
		REOPEN, DELETE_SOURCE_BRANCH, RESTORE_SOURCE_BRANCH};
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@Column(nullable=false)
	private Action action;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private User user;
	
	@Column(nullable=false)
	private Date date = new Date();

	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
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
