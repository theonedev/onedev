package io.onedev.server.util.facade;

import io.onedev.server.model.support.code.GitPackConfig;

import javax.annotation.Nullable;

public class ProjectFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;
	
	private final String name;
	
	private final String key;
	
	private final String path;
	
	private final String serviceDeskName;
	
	private final boolean codeManagement;
	
	private final boolean issueManagement;
	
	private final GitPackConfig gitPackConfig;
	
	private final Long lastEventDateId;
	
	private final Long defaultRoleId;
	
	private final boolean pendingDelete;
	
	private final Long parentId;
	
	public ProjectFacade(Long id, String name, @Nullable String key, String path,
						 @Nullable String serviceDeskName, boolean codeManagement, 
						 boolean issueManagement, GitPackConfig gitPackConfig,
						 Long lastEventDateId, @Nullable Long defaultRoleId, 
						 boolean pendingDelete, @Nullable Long parentId) {
		super(id);
		this.name = name;
		this.key = key;
		this.path = path;
		this.serviceDeskName = serviceDeskName;
		this.codeManagement = codeManagement;
		this.issueManagement = issueManagement;
		this.gitPackConfig = gitPackConfig;
		this.lastEventDateId = lastEventDateId;
		this.defaultRoleId = defaultRoleId;
		this.pendingDelete = pendingDelete;
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

	public boolean isPendingDelete() {
		return pendingDelete;
	}

	@Nullable
	public String getServiceDeskName() {
		return serviceDeskName;
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
