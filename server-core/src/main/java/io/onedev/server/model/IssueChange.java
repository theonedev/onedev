package io.onedev.server.model;

import java.util.Date;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;

import io.onedev.server.model.support.issue.changedata.IssueChangeData;

@Entity
@Table(indexes={
		@Index(columnList="o_issue_id"), @Index(columnList="o_user_id")})
public class IssueChange extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final int MAX_COMMENT_LEN = 15000;
	
	public static final String PROP_COMMENT = "comment";
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private Issue issue;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private User user;
	
	@Column(nullable=false)
	private Date date = new Date();
	
	@Column(length=MAX_COMMENT_LEN)
	private String comment;

	@Lob
	@Column(length=65535, nullable=false)
	private IssueChangeData data;

	public Issue getIssue() {
		return issue;
	}

	public void setIssue(Issue issue) {
		this.issue = issue;
	}

	@Nullable
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

	public IssueChangeData getData() {
		return data;
	}

	public void setData(IssueChangeData data) {
		this.data = data;
	}

	@Nullable
	public String getComment() {
		return comment;
	}

	public void setComment(@Nullable String comment) {
		if (comment != null)
			this.comment = StringUtils.abbreviate(comment, MAX_COMMENT_LEN);
		else
			this.comment = null;
	}

	public String getAnchor() {
		return getClass().getSimpleName() + "-" + getId();
	}
	
	public boolean affectsBoards() {
		return data.affectsListing();
	}

}
