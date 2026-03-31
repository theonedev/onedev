package io.onedev.server.workspace;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import io.onedev.k8shelper.CloneInfo;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;

public class WorkspaceContext implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final WorkspaceSpec spec;

	private final String token;

	private final Long projectId;
	
	private final String projectPath;

	private final String projectGitDir;
		
	private final Long workspaceId;
	
	private final Long workspaceNumber;

	private final Long userId;

	private final String userName;

	private final String userEmail;

	private final CloneInfo cloneInfo;

	private final String branch;

	public WorkspaceContext(WorkspaceSpec spec, String token, Long projectId, String projectPath, String projectGitDir, 
				Long workspaceId, Long workspaceNumber, Long userId, String userName, String userEmail, 
				CloneInfo cloneInfo, String branch) {
		this.spec = spec;
		this.token = token;
		this.projectId = projectId;
		this.projectPath = projectPath;
		this.projectGitDir = projectGitDir;
		this.workspaceId = workspaceId;
		this.workspaceNumber = workspaceNumber;
		this.userId = userId;
		this.userName = userName;
		this.userEmail = userEmail;
		this.cloneInfo = cloneInfo;
		this.branch = branch;
	}
	
	public WorkspaceSpec getSpec() {
		return spec;
	}

	public String getToken() {
		return token;
	}

	public Long getWorkspaceId() {
		return workspaceId;
	}

	public Long getProjectId() {
		return projectId;
	}

	public String getProjectPath() {
		return projectPath;
	}

	public String getProjectGitDir() {
		return projectGitDir;
	}

	public Long getWorkspaceNumber() {
		return workspaceNumber;
	}

	public Long getUserId() {
		return userId;
	}

	public String getUserName() {
		return userName;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public CloneInfo getCloneInfo() {
		return cloneInfo;
	}

	@Nullable
	public String getBranch() {
		return branch;
	}

}
