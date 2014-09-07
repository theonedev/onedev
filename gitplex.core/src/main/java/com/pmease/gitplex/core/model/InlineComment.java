package com.pmease.gitplex.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.util.diff.DiffLine;

@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"request", "commit", "file", "line"}) 
})
public class InlineComment extends AbstractEntity {
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private User user;

	@Column(nullable=false)
	private String commit;
	
	@Column(nullable=false)
	private String file;
	
	private int line;
	
	@Column(nullable=false)
	@Lob
	private List<DiffLine> context;
	
	@Column(nullable=false)
	private String content;
	
	@Column(nullable=false)
	private Date date;
	
	private boolean resolved;
	
	@OneToMany(mappedBy="comment", cascade=CascadeType.REMOVE)
	private Collection<InlineCommentReply> replies = new ArrayList<>();

	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getCommit() {
		return commit;
	}

	public void setCommit(String commit) {
		this.commit = commit;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public List<DiffLine> getContext() {
		return context;
	}

	public void setContext(List<DiffLine> context) {
		this.context = context;
	}

	public boolean isResolved() {
		return resolved;
	}

	public void setResolved(boolean resolved) {
		this.resolved = resolved;
	}

	public Collection<InlineCommentReply> getReplies() {
		return replies;
	}

	public void setReplies(Collection<InlineCommentReply> replies) {
		this.replies = replies;
	}
	
}
