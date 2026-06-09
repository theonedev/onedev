package io.onedev.server.workspace;

import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import io.onedev.k8shelper.CacheConfigFacade;
import io.onedev.k8shelper.CloneInfo;
import io.onedev.k8shelper.ConfigFileFacade;
import io.onedev.k8shelper.SetupScriptConfig;
import io.onedev.k8shelper.UserDataFacade;
import io.onedev.server.model.support.administration.workspaceprovisioner.WorkspaceProvisioner;
import io.onedev.server.model.support.workspace.spec.CacheConfig;
import io.onedev.server.model.support.workspace.spec.ConfigFile;
import io.onedev.server.model.support.workspace.spec.UserData;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;

public class WorkspaceContext implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final WorkspaceSpec spec;

	private final WorkspaceProvisioner provisioner;

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

	private final String commitHash;

	public WorkspaceContext(WorkspaceSpec spec, WorkspaceProvisioner provisioner, String token, 
				Long projectId, String projectPath, String projectGitDir, Long workspaceId, 
				Long workspaceNumber, Long userId, String userName, String userEmail, 
				CloneInfo cloneInfo, String commitHash, @Nullable String branch) {
		this.spec = spec;
		this.provisioner = provisioner;
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
		this.commitHash = commitHash;
		this.branch = branch;
	}
	
	public WorkspaceSpec getSpec() {
		return spec;
	}

	public WorkspaceProvisioner getProvisioner() {
		return provisioner;
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

	public String getCommitHash() {
		return commitHash;
	}

	public Map<String, String> getEnvVars() {
		var envVars = new LinkedHashMap<String, String>();
		for (var envVar : spec.getEnvVars())
			envVars.put(envVar.getName(), envVar.isSecret() ? envVar.getSecretValue() : envVar.getValue());
		return envVars;
	}

	public List<CacheConfigFacade> getCacheConfigFacades() {
		return spec.getCacheConfigs().stream()
				.map(CacheConfig::getFacade)
				.collect(toList());
	}

	public List<UserDataFacade> getUserDataFacades() {
		return spec.getUserDatas().stream()
				.map(UserData::getFacade)
				.collect(toList());
	}

	public List<ConfigFileFacade> getConfigFileFacades() {
		return spec.getConfigFiles().stream()
				.map(ConfigFile::getFacade)
				.collect(toList());
	}

	@Nullable
	public SetupScriptConfig getSetupScriptConfig() {
		var shell = spec.getShell();
		if (shell.getSetupCommands() != null) {
			return new SetupScriptConfig(
					shell.getFacility().normalizeCommands(shell.getSetupCommands()),
					shell.getFacility().getScriptExtension(),
					shell.getFacility().getExecutable(),
					shell.getFacility().getScriptOptions());
		} else {
			return null;
		}
	}

}
