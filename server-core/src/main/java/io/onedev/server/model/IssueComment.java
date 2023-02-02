package io.onedev.server.model;

import io.onedev.server.model.support.EntityComment;

import javax.persistence.*;

@Entity
@Table(indexes={
		@Index(columnList="o_issue_id"), @Index(columnList="o_user_id")})
public class IssueComment extends EntityComment {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(nullable=false)
	private Issue issue;

	public Issue getIssue() {
		return issue;
	}

	public void setIssue(Issue issue) {
		this.issue = issue;
	}

	@Override
	public AbstractEntity getEntity() {
		return getIssue();
	}
	
}
