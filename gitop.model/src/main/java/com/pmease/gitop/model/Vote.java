package com.pmease.gitop.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.pmease.commons.hibernate.AbstractEntity;

@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"voter", "update"})
})
public class Vote extends AbstractEntity {

	public static enum Result {
		APPROVE {
			@Override
			public boolean isApprove() {
				return true;
			}

			@Override
			public boolean isDisapprove() {
				return false;
			}
		}, 
		DISAPPROVE {
			@Override
			public boolean isApprove() {
				return false;
			}

			@Override
			public boolean isDisapprove() {
				return true;
			}
		};
		
		public abstract boolean isApprove();
		
		public abstract boolean isDisapprove();
	};
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private User voter;
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private PullRequestUpdate update;
	
	@Column(nullable=false)
	private Result result;

	public User getVoter() {
		return voter;
	}

	public void setVoter(User voter) {
		this.voter = voter;
	}
	
	public PullRequestUpdate getUpdate() {
		return update;
	}

	public void setUpdate(PullRequestUpdate update) {
		this.update = update;
	}

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}

}
