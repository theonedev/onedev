package io.onedev.server.model;

import io.onedev.server.model.support.TimeGroups;

import org.jspecify.annotations.Nullable;
import javax.persistence.*;
import java.util.Date;

import static io.onedev.server.model.IssueStateHistory.PROP_DATE;
import static io.onedev.server.model.IssueStateHistory.PROP_STATE;
import static io.onedev.server.model.support.TimeGroups.*;

@Entity
@Table(indexes={
		@Index(columnList="o_issue_id"), @Index(columnList=PROP_STATE), 
		@Index(columnList=PROP_DATE), @Index(columnList=PROP_MONTH), 
		@Index(columnList=PROP_WEEK), @Index(columnList=PROP_DAY),
})
public class IssueStateHistory extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROP_ISSUE = "issue";
	
	public static final String PROP_STATE = "state";
	
	public static final String PROP_DATE = "date";
	
	public static final String PROP_DURATION = "duration";
	
	public static final String PROP_TIME_GROUPS = "timeGroups";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Issue issue;
	
	@Column(nullable=false)
	private String state;

	@Column(nullable=false)
	private Date date;
	
	private Long duration;

	@Embedded
	private TimeGroups timeGroups;

	public Issue getIssue() {
		return issue;
	}

	public void setIssue(Issue issue) {
		this.issue = issue;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Nullable
	public Long getDuration() {
		return duration;
	}

	public void setDuration(@Nullable Long duration) {
		this.duration = duration;
	}

	public TimeGroups getTimeGroups() {
		return timeGroups;
	}

	public void setTimeGroups(TimeGroups timeGroups) {
		this.timeGroups = timeGroups;
	}
}
