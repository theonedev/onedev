package com.pmease.gitplex.core.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.pmease.commons.hibernate.AbstractEntity;

@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"g_reviewer_id", "g_request_id"})
})
public class ReviewInvitation extends AbstractEntity {

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User reviewer;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	private boolean perferred = true;
	
	@Column(nullable=false)
	private Date date = new Date();
	
	public User getReviewer() {
		return reviewer;
	}

	public void setReviewer(User reviewer) {
		this.reviewer = reviewer;
	}
	
	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	public boolean isPreferred() {
		return perferred;
	}

	public void setPerferred(boolean preferred) {
		this.perferred = preferred;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

}
