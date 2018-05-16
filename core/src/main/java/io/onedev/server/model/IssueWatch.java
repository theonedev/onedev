package io.onedev.server.model;

import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
		indexes={@Index(columnList="g_issue_id"), @Index(columnList="g_user_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"g_issue_id", "g_user_id"})
})
public class IssueWatch extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Issue issue;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;
	
	private String reason;
	
	private boolean ignore;
	
	public Issue getIssue() {
		return issue;
	}

	public void setIssue(Issue issue) {
		this.issue = issue;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Nullable
	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public boolean isIgnore() {
		return ignore;
	}

	public void setIgnore(boolean ignore) {
		this.ignore = ignore;
	}

}
