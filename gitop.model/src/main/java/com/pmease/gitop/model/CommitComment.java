package com.pmease.gitop.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.pmease.commons.hibernate.AbstractEntity;

@SuppressWarnings("serial")
@Entity
public class CommitComment extends AbstractEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(nullable = false)
//	@Index(name = "IDX_COMMENT_AUTHOR_ID")
	private User author;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(nullable = false)
//	@Index(name = "IDX_COMMENT_PROJECT_ID")
	private Project project;
	
	// Always use commit sha
	//
	@Column(nullable = false, length=40)
//	@Index(name = "IDX_COMMENT_COMMIT")
	private String commit;
	
	// when line is null means this is a commit comment, otherwise, this is 
	// a line comment
	@Column(nullable = true)
	private String line;
	
	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate = new Date();
	
	@Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDate = new Date();

	@Column
	@Lob
	@NotEmpty
	private String content;

	public static String buildLineId(String fileSha, int hunkIndex, int linePos) {
		return fileSha + "-L" + hunkIndex + "-" + linePos;
	}
	
	public boolean isLineComment() {
		return !Strings.isNullOrEmpty(getLine());
	}
	
	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public String getCommit() {
		return commit;
	}

	public void setCommit(String commit) {
		this.commit = commit;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("id", getId())
				.add("project", getProject())
				.add("commit", commit)
				.add("line", line)
				.add("content", content)
				.add("created", createdDate)
				.add("updatedDate", updatedDate)
				.toString();
	}
}
