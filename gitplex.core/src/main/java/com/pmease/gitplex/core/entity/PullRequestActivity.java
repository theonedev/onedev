package com.pmease.gitplex.core.entity;

import java.util.Date;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.gitplex.core.entity.component.PullRequestEvent;

@Entity
public class PullRequestActivity extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@Column(nullable=false)
	private PullRequestEvent event;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private Account user;
	
	@Column(nullable=false)
	private Date date = new Date();

	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	public PullRequestEvent getEvent() {
		return event;
	}

	public void setEvent(PullRequestEvent event) {
		this.event = event;
	}

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

	public void setDate(Date date) {
		this.date = date;
	}
	
}
