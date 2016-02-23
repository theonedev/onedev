package com.pmease.gitplex.core.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.pmease.commons.hibernate.AbstractEntity;

@SuppressWarnings("serial")
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"g_referenced_id", "g_referencedBy_id"})
})
@Entity
public class PullRequestReference extends AbstractEntity {

	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.JOIN)
	@JoinColumn(nullable=false)
	private PullRequest referenced;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.JOIN)
	@JoinColumn(nullable=false)
	private PullRequest referencedBy;
	
	@Column(nullable=false)
	private Date date = new Date();
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;

	public PullRequest getReferenced() {
		return referenced;
	}

	public void setReferenced(PullRequest referenced) {
		this.referenced = referenced;
	}

	public PullRequest getReferencedBy() {
		return referencedBy;
	}

	public void setReferencedBy(PullRequest referencedBy) {
		this.referencedBy = referencedBy;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
}
