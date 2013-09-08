package com.pmease.gitop.core.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.pmease.commons.hibernate.AbstractEntity;

@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"reviewer", "request"})
})
public class VoteInvitation extends AbstractEntity {

	@ManyToOne
	@JoinColumn(nullable=false)
	private User reviewer;
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private MergeRequest request;
	
	public User getReviewer() {
		return reviewer;
	}

	public void setReviewer(User reviewer) {
		this.reviewer = reviewer;
	}
	
	public MergeRequest getRequest() {
		return request;
	}

	public void setRequest(MergeRequest request) {
		this.request = request;
	}

}
