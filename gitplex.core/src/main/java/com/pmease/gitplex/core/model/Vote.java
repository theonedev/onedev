package com.pmease.gitplex.core.model;

import java.util.Date;

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

	public static enum Result {APPROVE, DISAPPROVE};
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private User voter;

	@Column(nullable=false)
	private Date date = new Date();
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private PullRequestUpdate update;
	
	@Column(nullable=false)
	private Result result;
	
	private String comment;

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

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

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
