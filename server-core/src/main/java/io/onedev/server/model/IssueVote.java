package io.onedev.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.onedev.server.rest.annotation.Immutable;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		indexes={@Index(columnList="issue_id"), @Index(columnList="user_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"issue_id", "user_id"})}
)
public class IssueVote extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(nullable=false)
	@Immutable
	private Issue issue;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	@Immutable
	private User user;
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Column(nullable=false)
	private Date date = new Date();
	
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

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getAnchor() {
		return getClass().getSimpleName() + "-" + getId();
	}

}
