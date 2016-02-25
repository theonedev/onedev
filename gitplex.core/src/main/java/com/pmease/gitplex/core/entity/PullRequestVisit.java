package com.pmease.gitplex.core.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.pmease.commons.hibernate.AbstractEntity;

@Entity
@Table(uniqueConstraints={@UniqueConstraint(columnNames={"g_request_id", "g_user_id"})})
public class PullRequestVisit extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;
	
	@Column(nullable=false)
	private Date date = new Date();

	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
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
