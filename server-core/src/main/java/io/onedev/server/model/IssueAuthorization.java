package io.onedev.server.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(
		indexes={@Index(columnList="o_user_id"), @Index(columnList="o_issue_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_user_id", "o_issue_id"})
})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class IssueAuthorization extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static String PROP_ISSUE = "issue";
	
	public static String PROP_USER = "user";
	
	@ManyToOne(fetch=FetchType.LAZY)
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
