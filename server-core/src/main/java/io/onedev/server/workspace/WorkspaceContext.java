package io.onedev.server.workspace;

import java.io.Serializable;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import io.onedev.k8shelper.CloneInfo;

public class WorkspaceContext implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final String token;

	private final Long projectId;
	
	private final String projectPath;

	private final String projectGitDir;
		
	private final Long workspaceId;
	
	private final Long workspaceNumber;

	private final String shell;

	private final Map<String, String> envVars;

	private final CloneInfo cloneInfo;

	private final boolean retrieveLfs;

	private final String branch;

	private final String userName;

	private final String userEmail;

	private final Map<String, String> userConfigs;

	public WorkspaceContext(String token, Long projectId, String projectPath, String projectGitDir, 
				Long workspaceId, Long workspaceNumber, String shell, Map<String, String> envVars, 
				CloneInfo cloneInfo, boolean retrieveLfs, @Nullable String branch, String userName, 
				String userEmail, Map<String, String> userConfigs) {
		this.token = token;
		this.projectId = projectId;
		this.projectPath = projectPath;
		this.projectGitDir = projectGitDir;
		this.workspaceId = workspaceId;
		this.workspaceNumber = workspaceNumber;
		this.shell = shell;
		this.envVars = envVars;
		this.cloneInfo = cloneInfo;
		this.retrieveLfs = retrieveLfs;
		this.branch = branch;
		this.userName = userName;
		this.userEmail = userEmail;
		this.userConfigs = userConfigs;
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

	@Nullable
	public String getShell() {
		return shell;
	}

	public Map<String, String> getEnvVars() {
		return envVars;
	}

	public CloneInfo getCloneInfo() {
		return cloneInfo;
	}

	public boolean isRetrieveLfs() {
		return retrieveLfs;
	}

	@Nullable
	public String getBranch() {
		return branch;
	}

	public String getUserName() {
		return userName;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public Map<String, String> getUserConfigs() {
		return userConfigs;
	}

}
