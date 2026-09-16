package io.onedev.server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import io.onedev.server.model.support.EntityWatch;
import io.onedev.server.rest.annotation.Immutable;

@Entity
@Table(
		indexes={@Index(columnList="issue_id"), @Index(columnList="user_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"issue_id", "user_id"})
})
public class IssueWatch extends EntityWatch {

	private static final long serialVersionUID = 1L;

	public static final String PROP_ISSUE = "issue";
	
	public static final String PROP_USER = "user";
	
	public static final String PROP_WATCHING = "watching";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	@Immutable
	private Issue issue;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	@Immutable
	private User user;
	
	private boolean watching;
	
	public Issue getIssue() {
		return issue;
	}

	public void setIssue(Issue issue) {
		this.issue = issue;
	}
	
	@Override
	public Issue getEntity() {
		return getIssue();
	}

	@Override
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public boolean isWatching() {
		return watching;
	}

	@Override
	public void setWatching(boolean watching) {
		this.watching = watching;
	}

}
