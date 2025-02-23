package io.onedev.server.util.facade;

import javax.annotation.Nullable;

import io.onedev.server.model.support.code.GitPackConfig;

public class ProjectFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;
	
	private final String name;
	
	private final String key;
	
	private final String path;
	
	private final String serviceDeskEmailAddress;
	
	private final boolean codeManagement;
	
	private final boolean issueManagement;
	
	private final GitPackConfig gitPackConfig;
	
	private final Long lastEventDateId;
	
	private final Long defaultRoleId;
	
	private final Long parentId;
	
	public ProjectFacade(Long id, String name, @Nullable String key, String path,
						 @Nullable String serviceDeskEmailAddress, boolean codeManagement,
						 boolean issueManagement, GitPackConfig gitPackConfig,
						 Long lastEventDateId, @Nullable Long defaultRoleId,
						 @Nullable Long parentId) {
		super(id);
		this.name = name;
		this.key = key;
		this.path = path;
		this.serviceDeskEmailAddress = serviceDeskEmailAddress;
		this.codeManagement = codeManagement;
		this.issueManagement = issueManagement;
		this.gitPackConfig = gitPackConfig;
		this.lastEventDateId = lastEventDateId;
		this.defaultRoleId = defaultRoleId;
		this.parentId = parentId;
	}

	public String getName() {
		return name;
	}

	@Nullable
	public String getKey() {
		return key;
	}

	public String getPath() {
		return path;
	}

	public boolean isCodeManagement() {
		return codeManagement;
	}

	public boolean isIssueManagement() {
		return issueManagement;
	}

	@Nullable
	public String getServiceDeskEmailAddress() {
		return serviceDeskEmailAddress;
	}

	public GitPackConfig getGitPackConfig() {
		return gitPackConfig;
	}

	public Long getLastEventDateId() {
		return lastEventDateId;
	}

	@Nullable
	public Long getDefaultRoleId() {
		return defaultRoleId;
	}

	@Nullable
	public Long getParentId() {
		return parentId;
	}

}
