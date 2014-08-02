package com.pmease.gitplex.core.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.pmease.commons.hibernate.AbstractEntity;

@SuppressWarnings("serial")
@Entity
public class OldCommitComment extends AbstractEntity {

	@ManyToOne
	@JoinColumn(nullable = false)
	private User author;
	
	@ManyToOne
	@JoinColumn(nullable = false)
	private Repository repository;
	
	@Column(nullable = false, length=40)
	@Index(name = "IDX_COMMENT_COMMIT")
	private String commit;
	
	// when line is null means this is a commit comment, otherwise, this is 
	// a line comment
	@Column(nullable = true)
	private String line;
	
	@Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate = new Date();

	@Column(nullable=false)
	@Lob
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

	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
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

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
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
				.add("repository", getRepository())
				.add("commit", commit)
				.add("line", line)
				.add("content", content)
				.add("updateDate", updateDate)
				.toString();
	}
}
