package io.onedev.server.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Stack;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.web.util.WicketUtils;

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
		this.commitId = commitId;
	}

	public Project getProject() {
		return OneDev.getInstance(ProjectManager.class).load(projectId);
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
	
}
