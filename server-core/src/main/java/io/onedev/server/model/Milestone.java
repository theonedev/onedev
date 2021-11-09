package io.onedev.server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.StringUtils;
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

	public static final int MAX_DESCRIPTION_LEN = 15000;
	
	public static final String PROP_ID = "id";
	
	public static final String PROP_PROJECT = "project";
	
	public static final String PROP_NAME = "name";
	
	public static final String PROP_START_DATE = "startDate";
	
	public static final String PROP_DUE_DATE = "dueDate";
	
	public static final String PROP_CLOSED = "closed";
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private Project project;
	
	@Column(nullable=false)
	private String name;
	
	@Column(length=MAX_DESCRIPTION_LEN)
	private String description;
	
	private Date startDate;
	
	private Date dueDate;
	
	private boolean closed;

	@OneToMany(mappedBy="milestone", cascade=CascadeType.REMOVE)
	private Collection<IssueSchedule> schedules = new ArrayList<>();
	
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
		this.description = StringUtils.abbreviate(description, MAX_DESCRIPTION_LEN);
	}

	@Editable(order=250, description="Optionally specify start date of the milestone")
	@Nullable
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
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

	public Collection<IssueSchedule> getSchedules() {
		return schedules;
	}

	public void setSchedules(Collection<IssueSchedule> schedules) {
		this.schedules = schedules;
	}

	public Map<String, Integer> getStateStats(Project tree) {
		Map<String, Integer> stateStats = new HashMap<>();
		for (IssueSchedule schedule: getSchedules()) {
			if (tree.isSelfOrAncestorOf(schedule.getIssue().getProject())) {
				Integer count = stateStats.get(schedule.getIssue().getState());
				if (count != null)
					count++;
				else
					count = 1;
				stateStats.put(schedule.getIssue().getState(), count);
			}
		}
		return stateStats;
	}
	
	public static class DatesAndStatusComparator extends DatesComparator {

		@Override
		public int compare(Milestone o1, Milestone o2) {
			if (o1.isClosed()) {
				if (o2.isClosed())
					return super.compare(o1, o2) * -1;
				else
					return 1;
			} else if (o2.isClosed()) {
				return -1;
			} else {
				return super.compare(o1, o2);
			}
		}
		
	}
	
	public static class DatesComparator implements Comparator<Milestone> {

		@Override
		public int compare(Milestone o1, Milestone o2) {
			if (o1.getStartDate() != null) {
				if (o2.getStartDate() != null)
					return o1.getStartDate().compareTo(o2.getStartDate());
				else
					return -1;
			} else {
				if (o2.getStartDate() != null) {
					return 1;
				} else {
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
			}
		}
		
	};			

}
