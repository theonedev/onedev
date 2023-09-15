package io.onedev.server.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.annotation.Nullable;
import javax.persistence.*;

import static io.onedev.server.model.IssueWork.PROP_DAY;

@Entity
@Table(indexes={@Index(columnList="o_issue_id"), @Index(columnList="o_user_id"), @Index(columnList=PROP_DAY)})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class IssueWork extends AbstractEntity {
	
	private static final long serialVersionUID = 1L;
	
	public static final String PROP_DAY = "day";

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Issue issue;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;
	
	private Integer day;
	
	private int minutes;
	
	private String description;

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

	public Integer getDay() {
		return day;
	}

	public void setDay(Integer day) {
		this.day = day;
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	@Nullable
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
