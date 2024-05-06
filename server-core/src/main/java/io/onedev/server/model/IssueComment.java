package io.onedev.server.model;

import io.onedev.server.model.support.EntityComment;
import io.onedev.server.rest.annotation.Immutable;

import javax.persistence.*;

@Entity
@Table(indexes={
		@Index(columnList="o_issue_id"), @Index(columnList="o_user_id")})
public class IssueComment extends EntityComment {

	private static final long serialVersionUID = 1L;

	public static final String PROP_ISSUE = "issue";
	
	@ManyToOne
	@JoinColumn(nullable=false)
	@Immutable
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
