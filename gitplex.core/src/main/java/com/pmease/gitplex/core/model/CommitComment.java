package com.pmease.gitplex.core.model;

import java.util.Date;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.google.common.base.Objects;
import com.pmease.commons.hibernate.AbstractEntity;

@SuppressWarnings("serial")
@Entity
public class CommitComment extends AbstractEntity {
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private Repository repository;
	
	@Column(nullable=false)
	private String commit;
	
	@Column(nullable=false)
	private Date commitDate;
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private User user;
	
	@Column(nullable=false)
	private Date commentDate = new Date();
	
	@Column(nullable=false)
	private String content;
	
	@Embedded
	private CommentPosition position;
	
	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getCommitDate() {
		return commitDate;
	}

	public void setCommitDate(Date commitDate) {
		this.commitDate = commitDate;
	}

	public Date getCommentDate() {
		return commentDate;
	}

	public void setCommentDate(Date commentDate) {
		this.commentDate = commentDate;
	}

	public String getCommit() {
		return commit;
	}

	public void setCommit(String commit) {
		this.commit = commit;
	}

	@Nullable
	public CommentPosition getPosition() {
		return position;
	}

	public void setPosition(@Nullable CommentPosition position) {
		this.position = position;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(CommitComment.class)
				.add("commit", commit)
				.add("position", position)
				.add("content", content)
				.toString();
	}
}
