package com.gitplex.server.core.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.gitplex.commons.hibernate.AbstractEntity;

@Entity
@Table(
		indexes={@Index(columnList="g_user_id"), @Index(columnList="g_update_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"g_user_id", "g_update_id"})
})
public class PullRequestReview extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static enum Result {APPROVE, DISAPPROVE};
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Account user;

	@Column(nullable=false)
	private Date date;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private PullRequestUpdate update;
	
	@Column(nullable=false)
	private Result result;
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Account getUser() {
		return user;
	}

	public void setUser(Account user) {
		this.user = user;
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
