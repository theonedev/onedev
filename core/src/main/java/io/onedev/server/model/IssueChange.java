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

@Entity
@Table(indexes={
		@Index(columnList="g_issue_id"), @Index(columnList="g_user_id")})
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
	
	@Column(nullable=false)
	private String property;
	
	@Lob
	@Column(length=65535)
	private String prevContent;

	@Lob
	@Column(length=65535)
	private String content;
	
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

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getPrevContent() {
		return prevContent;
	}

	public void setPrevContent(String prevContent) {
		this.prevContent = prevContent;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getAnchor() {
		return getClass().getSimpleName() + "-" + getId();
	}

}
