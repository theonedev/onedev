package io.onedev.server.util;

import java.io.Serial;
import java.io.Serializable;
import java.util.Stack;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jspecify.annotations.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.model.Project;
import io.onedev.server.service.ProjectService;

public class ProjectScopedBranch implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private static final ThreadLocal<Stack<ProjectScopedBranch>> stack = new ThreadLocal<Stack<ProjectScopedBranch>>() {

		@Override
		protected Stack<ProjectScopedBranch> initialValue() {
			return new Stack<>();
		}

	};

	private final Long projectId;

	private final String branchName;

	public ProjectScopedBranch(Project project, String branchName) {
		this(project.getId(), branchName);
	}

	public ProjectScopedBranch(Long projectId, String branchName) {
		this.projectId = projectId;
		this.branchName = branchName;
	}

	public Project getProject() {
		return OneDev.getInstance(ProjectService.class).load(projectId);
	}

	public Long getProjectId() {
		return projectId;
	}

	public String getBranchName() {
		return branchName;
	}

	public static void push(ProjectScopedBranch branch) {
		stack.get().push(branch);
	}

	public static void pop() {
		stack.get().pop();
	}

	@Nullable
	public static ProjectScopedBranch get() {
		if (!stack.get().isEmpty())
			return stack.get().peek();
		else
			return null;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ProjectScopedBranch))
			return false;
		if (this == other)
			return true;
		ProjectScopedBranch otherBranch = (ProjectScopedBranch) other;
		return new EqualsBuilder()
				.append(projectId, otherBranch.projectId)
				.append(branchName, otherBranch.branchName)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(projectId).append(branchName).toHashCode();
	}

	@Override
	public String toString() {
		return getProject().getPath() + ":" + branchName;
	}

}
