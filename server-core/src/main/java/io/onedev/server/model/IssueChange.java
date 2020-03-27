package io.onedev.server.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import io.onedev.server.model.support.issue.changedata.IssueChangeData;

@Entity
@Table(indexes={
		@Index(columnList="o_issue_id"), @Index(columnList="o_user_id")})
public class IssueChange extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(nullable=false)
	private Issue issue;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn
	private User user;
	
	private String userName;
	
	@Column(nullable=false)
	private Date date;

	@Lob
	@Column(length=65535, nullable=false)
	private IssueChangeData data;

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

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
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

	public String getAnchor() {
		return getClass().getSimpleName() + "-" + getId();
	}
	
	public boolean affectsBoards() {
		return data.affectsBoards();
	}

}
