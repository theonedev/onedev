package io.onedev.server.model;

import javax.persistence.*;

@Entity
@Table(
		indexes={@Index(columnList="o_issue_id"), @Index(columnList="o_user_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_issue_id", "o_user_id"})}
)
public class IssueMention extends AbstractEntity {

	public static final String PROP_USER = "user";
	
	public static final String PROP_ISSUE = "issue";
	
	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(nullable=false)
	private Issue issue;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;
	
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
	
}
