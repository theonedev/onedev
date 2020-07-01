package io.onedev.server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Multiline;

@Entity
@Table(
		indexes={@Index(columnList="o_project_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_project_id", Milestone.PROP_NAME})}
)
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Editable
public class Milestone extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROP_ID = "id";
	
	public static final String PROP_NAME = "name";
	
	public static final String PROP_DUE_DATE = "dueDate";
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private Project project;
	
	@Column(nullable=false)
	private String name;
	
	private String description;
	
	private Date dueDate;
	
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

	@Editable(order=300, description="Optionally specify due date of the milestone")
	@Nullable
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
	
	public String getStatusName() {
		return closed?"Closed":"Open";
	}

	public Collection<Issue> getIssues() {
		return issues;
	}

	public void setIssues(Collection<Issue> issues) {
		this.issues = issues;
	}
	
	public Map<String, Integer> getStateStats() {
		Map<String, Integer> stateStats = new HashMap<>();
		for (Issue issue: getIssues()) {
			Integer count = stateStats.get(issue.getState());
			if (count != null)
				count++;
			else
				count = 1;
			stateStats.put(issue.getState(), count);
		}
		return stateStats;
	}
	
	public static class DueDateComparator implements Comparator<Milestone> {

		@Override
		public int compare(Milestone o1, Milestone o2) {
			if (o1.getDueDate() != null) {
				if (o2.getDueDate() != null)
					return o1.getDueDate().compareTo(o2.getDueDate());
				else
					return -1;
			} else {
				if (o2.getDueDate() != null)
					return 1;
				else
					return o1.getName().compareTo(o2.getName());
			}
		}
		
	};			

}
