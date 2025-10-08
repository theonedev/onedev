package io.onedev.server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;
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

import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.annotation.Immutable;

@Entity
@Table(
		indexes={@Index(columnList="o_project_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_project_id", Iteration.PROP_NAME})}
)
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class Iteration extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final int MAX_DESCRIPTION_LEN = 15000;
	
	public static final String PROP_PROJECT = "project";
	
	public static final String PROP_NAME = "name";
	
	public static final String PROP_START_DAY = "startDay";
	
	public static final String PROP_DUE_DAY = "dueDay";
	
	public static final String PROP_CLOSED = "closed";
	
	@ManyToOne
	@JoinColumn(nullable=false)
	@Immutable
	private Project project;
	
	@Column(nullable=false)
	private String name;
	
	@Api(description="Description of the iteration. May be empty")
	@Column(length=MAX_DESCRIPTION_LEN)
	private String description;
	
	@Api(description="Start of the iteration in epoc day. May be null")
	private Long startDay;
	
	@Api(description="Due of the iteration in epoc day. May be null")
	private Long dueDay;
	
	private boolean closed;

	@OneToMany(mappedBy= "iteration", cascade=CascadeType.REMOVE)
	private Collection<IssueSchedule> schedules = new ArrayList<>();
	
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = StringUtils.abbreviate(description, MAX_DESCRIPTION_LEN);
	}

	@Nullable
	public Long getStartDay() {
		return startDay;
	}

	public void setStartDay(Long startDay) {
		this.startDay = startDay;
	}

	@Nullable
	public Long getDueDay() {
		return dueDay;
	}

	public void setDueDay(Long dueDay) {
		this.dueDay = dueDay;
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
		public int compare(Iteration o1, Iteration o2) {
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
	
	public static class DatesComparator implements Comparator<Iteration> {

		@Override
		public int compare(Iteration o1, Iteration o2) {
			if (o1.getStartDay() != null) {
				if (o2.getStartDay() != null)
					return o1.getStartDay().compareTo(o2.getStartDay());
				else
					return -1;
			} else {
				if (o2.getStartDay() != null) {
					return 1;
				} else {
					if (o1.getDueDay() != null) {
						if (o2.getDueDay() != null)
							return o1.getDueDay().compareTo(o2.getDueDay());
						else
							return -1;
					} else {
						if (o2.getDueDay() != null)
							return 1;
						else
							return o1.getName().compareTo(o2.getName());
					}
				}
			}
		}
		
	};			

}
