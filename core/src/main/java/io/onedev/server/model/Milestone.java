package io.onedev.server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Multiline;

@Entity
@Table(
		indexes={@Index(columnList="o_project_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_project_id", "name"})}
)
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Editable
public class Milestone extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String FIELD_ATTR_NAME = "name";
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private Project project;
	
	@Column(nullable=false)
	private String name;
	
	private String description;
	
	@Column(nullable=false)
	private Date dueDate;
	
	private int numOfOpenIssues;
	
	private int numOfClosedIssues;
	
	private boolean closed;

	@OneToMany(mappedBy="milestone")
	private Collection<Issue> issues = new ArrayList<>();
	
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@Editable(order=100)
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200)
	@Multiline
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Editable(order=300)
	@NotNull(message="may not be empty")
	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public int getNumOfOpenIssues() {
		return numOfOpenIssues;
	}

	public void setNumOfOpenIssues(int numOfOpenIssues) {
		this.numOfOpenIssues = numOfOpenIssues;
	}

	public int getNumOfClosedIssues() {
		return numOfClosedIssues;
	}

	public void setNumOfClosedIssues(int numOfClosedIssues) {
		this.numOfClosedIssues = numOfClosedIssues;
	}

	public Collection<Issue> getIssues() {
		return issues;
	}

	public void setIssues(Collection<Issue> issues) {
		this.issues = issues;
	}
	
}
