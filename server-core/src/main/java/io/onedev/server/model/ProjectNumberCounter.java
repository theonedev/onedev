package io.onedev.server.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/** Counters are separate from Project to avoid upgrading foreign-key locks on project rows. */
@Entity
@Table
public class ProjectNumberCounter extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROP_NEXT_ISSUE_NUMBER = "nextIssueNumber";

	public static final String PROP_NEXT_PULL_REQUEST_NUMBER = "nextPullRequestNumber";

	public static final String PROP_NEXT_BUILD_NUMBER = "nextBuildNumber";

	public static final String PROP_NEXT_WORKSPACE_NUMBER = "nextWorkspaceNumber";

	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false, unique=true)
	@OnDelete(action=OnDeleteAction.CASCADE)
	private Project project;

	// Updated monotonically outside Hibernate's managed entity state.
	@Column(nullable=false, updatable=false)
	private long nextIssueNumber = 1;

	@Column(nullable=false, updatable=false)
	private long nextPullRequestNumber = 1;

	@Column(nullable=false, updatable=false)
	private long nextBuildNumber = 1;

	@Column(nullable=false, updatable=false)
	private long nextWorkspaceNumber = 1;

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public long getNextIssueNumber() {
		return nextIssueNumber;
	}

	public long getNextPullRequestNumber() {
		return nextPullRequestNumber;
	}

	public long getNextBuildNumber() {
		return nextBuildNumber;
	}

	public long getNextWorkspaceNumber() {
		return nextWorkspaceNumber;
	}

}
