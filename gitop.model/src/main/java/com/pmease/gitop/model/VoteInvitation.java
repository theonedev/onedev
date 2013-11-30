package com.pmease.gitop.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.pmease.commons.hibernate.AbstractEntity;

@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"voter", "request"})
})
public class VoteInvitation extends AbstractEntity {

	@ManyToOne
	@JoinColumn(nullable=false)
	private User voter;
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	public User getVoter() {
		return voter;
	}

	public void setVoter(User voter) {
		this.voter = voter;
	}
	
	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

}
