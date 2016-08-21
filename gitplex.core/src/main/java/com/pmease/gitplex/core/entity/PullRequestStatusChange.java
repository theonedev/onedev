package com.pmease.gitplex.core.entity;

import java.util.Date;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import org.hibernate.annotations.DynamicUpdate;

import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.gitplex.core.event.pullrequest.PullRequestStatusChangeEvent;

@Entity
@DynamicUpdate
public class PullRequestStatusChange extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@Version
	private long version;
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@Column(nullable=false)
	private Class<? extends PullRequestStatusChangeEvent> eventType;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Account user;
	
	@Column(nullable=false)
	private Date date;
	
	private String note;

	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	public Class<? extends PullRequestStatusChangeEvent> getEventType() {
		return eventType;
	}

	public void setEventType(Class<? extends PullRequestStatusChangeEvent> eventType) {
		this.eventType = eventType;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@Nullable
	public Account getUser() {
		return user;
	}

	public void setUser(Account user) {
		this.user = user;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}
	
}
