package com.pmease.gitplex.core.model;

import java.util.Date;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.pmease.commons.hibernate.AbstractEntity;

@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"repository", "commit", "filePath", "lineNo"}) 
})
public class CommentVisit extends AbstractEntity {
	
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
	private Date visitDate = new Date();

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

	public Date getVisitDate() {
		return visitDate;
	}

	public void setVisitDate(Date date) {
		this.visitDate = date;
	}

	public String getCommit() {
		return commit;
	}

	public void setCommit(String commit) {
		this.commit = commit;
	}

	public Date getCommitDate() {
		return commitDate;
	}

	public void setCommitDate(Date commitDate) {
		this.commitDate = commitDate;
	}

	@Nullable
	public CommentPosition getPosition() {
		return position;
	}

	public void setPosition(@Nullable CommentPosition position) {
		this.position = position;
	}

}
