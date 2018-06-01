package io.onedev.server.model;

import java.util.ArrayList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
		indexes={@Index(columnList="g_project_id"), @Index(columnList="g_milestone_id"), @Index(columnList="name")},
		uniqueConstraints={@UniqueConstraint(columnNames={"g_project_id", "name"})}
)
public class IssueBoard extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;

	@ManyToOne(fetch=FetchType.LAZY)
	private Milestone milestone;
	
	@Column(nullable=false)
	private String name;
	
	private String query;
	
	@Column(nullable=false)
	private String columnField;
	
	@Lob
	private ArrayList<String> columns = new ArrayList<>();

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Milestone getMilestone() {
		return milestone;
	}

	public void setMilestone(Milestone milestone) {
		this.milestone = milestone;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getColumnField() {
		return columnField;
	}

	public void setColumnField(String columnField) {
		this.columnField = columnField;
	}

	public ArrayList<String> getColumns() {
		return columns;
	}

	public void setColumns(ArrayList<String> columns) {
		this.columns = columns;
	}
	
}
