package com.gitplex.server.model;

import java.util.Date;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Table(
		indexes={@Index(columnList="g_referenced_id"), @Index(columnList="g_referencedBy_id"), @Index(columnList="g_user_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"g_referenced_id", "g_referencedBy_id"})
})
@Entity
public class PullRequestReference extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.JOIN)
	@JoinColumn(nullable=false)
	private PullRequest referenced;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.JOIN)
	@JoinColumn(nullable=false)
	private PullRequest referencedBy;
	
	@Column(nullable=false)
	private Date date;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn
	private Account user;
	
	private String userName;

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

	@Nullable
	public Account getUser() {
		return user;
	}

	public void setUser(Account user) {
		this.user = user;
	}

	@Nullable
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
}
