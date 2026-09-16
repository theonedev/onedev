package io.onedev.server.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(
		indexes={@Index(columnList="issue_id"), @Index(columnList="user_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"issue_id", "user_id"})})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class Stopwatch extends AbstractEntity {
	
	private static final long serialVersionUID = 1L;
	
	public static final String PROP_ISSUE = "issue";

	public static final String PROP_USER = "user";
	
	public static final String PROP_DATE = "date";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Issue issue;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;
	
	@Column(nullable = false)
	private Date date;
	
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

}
