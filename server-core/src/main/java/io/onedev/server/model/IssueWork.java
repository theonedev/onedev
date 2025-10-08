package io.onedev.server.model;

import static io.onedev.server.model.IssueWork.PROP_DATE;

import java.util.Date;

import org.jspecify.annotations.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.onedev.server.rest.annotation.Immutable;

@Entity
@Table(indexes={@Index(columnList="o_issue_id"), @Index(columnList="o_user_id"), @Index(columnList=PROP_DATE)})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class IssueWork extends AbstractEntity {

	private static final int MAX_CONTENT_LEN = 1000;
	
	private static final long serialVersionUID = 1L;

	public static final String PROP_USER = "user";
	
	public static final String PROP_ISSUE = "issue";
	
	public static final String PROP_DATE = "date";
		
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	@Immutable
	private Issue issue;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	@Immutable
	private User user;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Column(nullable = false)
	private Date date = new Date();
		
	private int minutes;

	@Column(length=MAX_CONTENT_LEN)
	private String note;

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

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	@Nullable
	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = StringUtils.abbreviate(note, MAX_CONTENT_LEN);		
	}

	public String getAnchor() {
		return getClass().getSimpleName() + "-" + getId();
	}

}
