package io.onedev.server.util;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.util.WicketUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.Stack;

public class ProjectScopedCommit implements Serializable {

	private static final long serialVersionUID = 1L;

	private static ThreadLocal<Stack<ProjectScopedCommit>> stack =  new ThreadLocal<Stack<ProjectScopedCommit>>() {

		@Override
		protected Stack<ProjectScopedCommit> initialValue() {
			return new Stack<>();
		}
	
	};

	private final Long projectId;
	
	private final ObjectId commitId;
	
	private transient Collection<Long> fixedIssueIds;
	
	public ProjectScopedCommit(Project project, ObjectId commitId) {
		this(project.getId(), commitId);
	}

	public ProjectScopedCommit(Long projectId, ObjectId commitId) {
		this.projectId = projectId;
		this.commitId = commitId.copy();
	}

	public Project getProject() {
		return getProject(true);
	}

	@Nullable
	public Project getProject(boolean mustExist) {
		ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
		if (mustExist)
			return projectManager.load(projectId);
		else 
			return projectManager.get(projectId);
	}
	
	public RevCommit getRevCommit() {
		return getProject().getRevCommit(commitId, true);
	}
	
	public Long getProjectId() {
		return projectId;
	}
	
	public ObjectId getCommitId() {
		return commitId;
	}

	public Collection<Long> getFixedIssueIds() {
		if (fixedIssueIds == null) {
			Project project = getProject();
			RevCommit revCommit = project.getRevCommit(commitId, true);
			fixedIssueIds = project.parseFixedIssueIds(revCommit.getFullMessage());
		}
		return fixedIssueIds;
	}
	
	@Nullable
	public static ProjectScopedCommit from(String revisionFQN) {
		ProjectScopedRevision revision = ProjectScopedRevision.from(revisionFQN);
		if (revision != null) {
			Project project = revision.getProject();
			return new ProjectScopedCommit(project, project.getObjectId(revision.getRevision(), false));
		} else {
			return null;
		}
	}
	
	public static void push(ProjectScopedCommit commitId) {
		stack.get().push(commitId);
	}

	public static void pop() {
		stack.get().pop();
	}
	
	@Nullable
	public static ProjectScopedCommit get() {
		if (!stack.get().isEmpty()) { 
			return stack.get().peek();
		} else {
			ComponentContext componentContext = ComponentContext.get();
			if (componentContext != null) {
				CommitAware commitAware = WicketUtils.findInnermost(componentContext.getComponent(), CommitAware.class);
				if (commitAware != null) 
					return commitAware.getCommit();
			}
			return null;
		}
	}
	
	public boolean canDisplay() {
		Project project = getProject(false);
		return project != null
				&& SecurityUtils.canReadCode(project)
				&& project.getRevCommit(commitId, false) != null;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ProjectScopedCommit))
			return false;
		if (this == other)
			return true;
		ProjectScopedCommit otherProjectScopedCommit = (ProjectScopedCommit) other;
		return new EqualsBuilder()
				.append(projectId, otherProjectScopedCommit.projectId)
				.append(commitId, otherProjectScopedCommit.commitId)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(projectId).append(commitId).toHashCode();
	}
	
	public String getFQN() {
		return getProject().getPath() + ":" + GitUtils.abbreviateSHA(getCommitId().name());
	}

	@Override
	public String toString() {
		return getFQN();
	}
	
}
