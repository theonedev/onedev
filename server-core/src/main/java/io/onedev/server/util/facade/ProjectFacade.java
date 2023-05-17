package io.onedev.server.util.facade;

import io.onedev.server.model.support.code.GitPackConfig;

import javax.annotation.Nullable;

public class ProjectFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;
	
	private final String name;
	
	private final String path;
	
	private final String serviceDeskName;
	
	private final boolean codeManagement;
	
	private final boolean issueManagement;
	
	private final GitPackConfig gitPackConfig;
	
	private final Long lastEventDateId;
	
	private final Long defaultRoleId;
	
	private final Long parentId;
	
	public ProjectFacade(Long id, String name, String path,
						 @Nullable String serviceDeskName, boolean codeManagement, 
						 boolean issueManagement, GitPackConfig gitPackConfig,
						 Long lastEventDateId, @Nullable Long defaultRoleId, 
						 @Nullable Long parentId) {
		super(id);
		this.name = name;
		this.path = path;
		this.serviceDeskName = serviceDeskName;
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
