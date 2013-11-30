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
		ACCEPT {
			@Override
			public boolean isAccept() {
				return true;
			}

			@Override
			public boolean isReject() {
				return false;
			}
		}, 
		REJECT {
			@Override
			public boolean isAccept() {
				return false;
			}

			@Override
			public boolean isReject() {
				return true;
			}
		};
		
		public abstract boolean isAccept();
		
		public abstract boolean isReject();
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
