package io.onedev.server.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Stack;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

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

	private final Project project;
	
	private final ObjectId commitId;
	
	private transient Collection<Long> fixedIssueNumbers;
	
	public ProjectScopedCommit(Project project, ObjectId commitId) {
		this.project = project;
		this.commitId = commitId;
	}

	public Project getProject() {
		return project;
	}

	public ObjectId getCommitId() {
		return commitId;
	}

	public Collection<Long> getFixedIssueNumbers() {
		if (fixedIssueNumbers == null) {
			RevCommit revCommit = project.getRevCommit(commitId, true);
			fixedIssueNumbers = IssueUtils.parseFixedIssueNumbers(project, revCommit.getFullMessage());
		}
		return fixedIssueNumbers;
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
