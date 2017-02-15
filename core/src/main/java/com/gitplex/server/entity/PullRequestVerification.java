package com.gitplex.server.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.gitplex.server.persistence.AbstractEntity;

@Entity
@Table(
		indexes={@Index(columnList="g_request_id"), @Index(columnList="g_user_id"), 
				@Index(columnList="commit"), @Index(columnList="configuration")},
		uniqueConstraints={@UniqueConstraint(columnNames={"g_request_id", "commit", "configuration"})
})
public class PullRequestVerification extends AbstractEntity {
	
	private static final long serialVersionUID = 1L;

	public enum Status {RUNNING, SUCCESSFUL, FAILED}
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Account user;
	
	@Column(nullable=false)
	private String commit;

	@Column(nullable=false)
	private String configuration;
	
	@Column(nullable=false)
	private String message;
	
	@Column(nullable=false)
	private Status status;
	
	private Date date;

	public String getCommit() {
		return commit;
	}

	public void setCommit(String commit) {
		this.commit = commit;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	public Account getUser() {
		return user;
	}

	public void setUser(Account user) {
		this.user = user;
	}

	public String getConfiguration() {
		return configuration;
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
}
