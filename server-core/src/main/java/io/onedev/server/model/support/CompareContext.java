package io.onedev.server.model.support;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import io.onedev.server.model.PullRequest;
import io.onedev.server.util.diff.WhitespaceOption;

@Embeddable
public class CompareContext implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String PROP_PULL_REQUEST = "pullRequest";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn
	private PullRequest pullRequest;
	
	@Nullable
	public PullRequest getPullRequest() {
		return pullRequest;
	}

	public void setPullRequest(PullRequest pullRequest) {
		this.pullRequest = pullRequest;
	}

	@Column(nullable=false)
	private String oldCommitHash;
	
	@Column(nullable=false)
	private String newCommitHash;
	
	private String pathFilter;
	
	private String currentFile;
	
	@Column(nullable=false)
	private WhitespaceOption whitespaceOption = WhitespaceOption.DEFAULT;

	public String getOldCommitHash() {
		return oldCommitHash;
	}

	public void setOldCommitHash(String oldCommitHash) {
		this.oldCommitHash = oldCommitHash;
	}

	public String getNewCommitHash() {
		return newCommitHash;
	}

	public void setNewCommitHash(String newCommitHash) {
		this.newCommitHash = newCommitHash;
	}

	@Nullable
	public String getPathFilter() {
		return pathFilter;
	}

	public void setPathFilter(@Nullable String pathFilter) {
		this.pathFilter = pathFilter;
	}

	@Nullable
	public String getCurrentFile() {
		return currentFile;
	}

	public void setCurrentFile(@Nullable String currentFile) {
		this.currentFile = currentFile;
	}

	public WhitespaceOption getWhitespaceOption() {
		return whitespaceOption;
	}

	public void setWhitespaceOption(WhitespaceOption whitespaceOption) {
		this.whitespaceOption = whitespaceOption;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof CompareContext))
			return false;
		if (this == other)
			return true;
		CompareContext otherContext = (CompareContext) other;
		return new EqualsBuilder()
				.append(pullRequest, otherContext.pullRequest)
				.append(oldCommitHash, otherContext.oldCommitHash)
				.append(newCommitHash, otherContext.newCommitHash)
				.append(pathFilter, otherContext.pathFilter)
				.append(currentFile, otherContext.currentFile)
				.append(whitespaceOption, otherContext.whitespaceOption)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(pullRequest)
				.append(oldCommitHash)
				.append(newCommitHash)
				.append(pathFilter)
				.append(currentFile)
				.append(whitespaceOption)
				.toHashCode();
	}
	
}
