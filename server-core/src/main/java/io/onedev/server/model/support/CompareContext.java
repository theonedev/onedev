package io.onedev.server.model.support;

import io.onedev.server.model.PullRequest;
import io.onedev.server.util.diff.WhitespaceOption;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import org.jspecify.annotations.Nullable;
import javax.persistence.*;
import java.io.Serializable;

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
	
	@Column(nullable=false)
	private WhitespaceOption whitespaceOption = WhitespaceOption.IGNORE_TRAILING;

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
				.append(whitespaceOption)
				.toHashCode();
	}
	
}
