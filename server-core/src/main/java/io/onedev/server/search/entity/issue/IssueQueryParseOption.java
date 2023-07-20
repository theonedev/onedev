package io.onedev.server.search.entity.issue;

import java.io.Serializable;

public class IssueQueryParseOption implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean withProjectCriteria = true;
	
	private boolean withCurrentProjectCriteria;
	
	private boolean withCurrentUserCriteria;
	
	private boolean withCurrentIssueCriteria;
	
	private boolean withCurrentBuildCriteria;
	
	private boolean withCurrentPullRequestCriteria;
	
	private boolean withCurrentCommitCriteria;
	
	private boolean withStateCriteria = true;
	
	private boolean withOrder = true;
	
	public boolean withProjectCriteria() {
		return withProjectCriteria;
	}
	
	public IssueQueryParseOption withProjectCriteria(boolean withProjectCriteria) {
		this.withProjectCriteria = withProjectCriteria;
		return this;
	}
	
	public boolean withCurrentProjectCriteria() {
		return withCurrentProjectCriteria;
	}

	public IssueQueryParseOption withCurrentProjectCriteria(boolean withCurrentProjectCriteria) {
		this.withCurrentProjectCriteria = withCurrentProjectCriteria;
		return this;
	}
	
	public boolean withCurrentUserCriteria() {
		return withCurrentUserCriteria;
	}

	public IssueQueryParseOption withCurrentUserCriteria(boolean withCurrentUserCriteria) {
		this.withCurrentUserCriteria = withCurrentUserCriteria;
		return this;
	}

	public boolean withCurrentIssueCriteria() {
		return withCurrentIssueCriteria;
	}

	public IssueQueryParseOption withCurrentIssueCriteria(boolean withCurrentIssueCriteria) {
		this.withCurrentIssueCriteria = withCurrentIssueCriteria;
		return this;
	}

	public boolean withCurrentBuildCriteria() {
		return withCurrentBuildCriteria;
	}

	public IssueQueryParseOption withCurrentBuildCriteria(boolean withCurrentBuildCriteria) {
		this.withCurrentBuildCriteria = withCurrentBuildCriteria;
		return this;
	}

	public boolean withCurrentPullRequestCriteria() {
		return withCurrentPullRequestCriteria;
	}

	public IssueQueryParseOption withCurrentPullRequestCriteria(boolean withCurrentPullRequestCriteria) {
		this.withCurrentPullRequestCriteria = withCurrentPullRequestCriteria;
		return this;
	}

	public boolean withCurrentCommitCriteria() {
		return withCurrentCommitCriteria;
	}

	public IssueQueryParseOption withCurrentCommitCriteria(boolean withCurrentCommitCriteria) {
		this.withCurrentCommitCriteria = withCurrentCommitCriteria;
		return this;
	}

	public boolean withStateCriteria() {
		return withStateCriteria;
	}
	
	public IssueQueryParseOption withStateCriteria(boolean withStateCriteria) {
		this.withStateCriteria = withStateCriteria;
		return this;
	}
	
	public boolean withOrder() {
		return withOrder;
	}

	public IssueQueryParseOption withOrder(boolean withOrder) {
		this.withOrder = withOrder;
		return this;
	}

	public IssueQueryParseOption enableAll(boolean enabled) {
		withCurrentProjectCriteria = enabled;
		withCurrentBuildCriteria = enabled;
		withCurrentCommitCriteria = enabled;
		withCurrentIssueCriteria = enabled;
		withCurrentPullRequestCriteria = enabled;
		withCurrentUserCriteria = enabled;
		withOrder = enabled;
		return this;
	}
	
}
