package io.onedev.server.model;

import java.util.Date;

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
		indexes={@Index(columnList="o_issue_id"), @Index(columnList="o_milestone_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_issue_id", "o_milestone_id"})
})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class IssueSchedule extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static String PROP_ISSUE = "issue";
	
	public static String NAME_MILESTONE = "Milestone";
	
	public static String PROP_MILESTONE = "milestone";
	
	public static String PROP_DATE = "date";

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Issue issue;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Milestone milestone;

	private Date date = new Date();

	public Issue getIssue() {
		return issue;
	}

	public void setIssue(Issue issue) {
		this.issue = issue;
	}

	public Milestone getMilestone() {
		return milestone;
	}

	public void setMilestone(Milestone milestone) {
		this.milestone = milestone;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
}
