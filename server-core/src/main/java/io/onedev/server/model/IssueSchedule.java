package io.onedev.server.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.Date;

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

	@ManyToOne
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
