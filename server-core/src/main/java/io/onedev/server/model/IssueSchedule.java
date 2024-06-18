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
		indexes={@Index(columnList="o_issue_id"), @Index(columnList="o_iteration_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_issue_id", "o_iteration_id"})
})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class IssueSchedule extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static String PROP_ISSUE = "issue";
	
	public static String NAME_ITERATION = "Iteration";
	
	public static String PROP_ITERATION = "iteration";
	
	public static String PROP_DATE = "date";

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Issue issue;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Iteration iteration;

	private Date date = new Date();

	public Issue getIssue() {
		return issue;
	}

	public void setIssue(Issue issue) {
		this.issue = issue;
	}

	public Iteration getIteration() {
		return iteration;
	}

	public void setIteration(Iteration iteration) {
		this.iteration = iteration;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
}
