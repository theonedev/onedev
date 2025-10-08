package io.onedev.server.util.facade;

import org.jspecify.annotations.Nullable;

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
		
	private final Long parentId;

	private final Long forkedFromId;
	
	public ProjectFacade(Long id, String name, @Nullable String key, String path,
						 @Nullable String serviceDeskEmailAddress, boolean codeManagement,
						 boolean issueManagement, GitPackConfig gitPackConfig,
						 Long lastEventDateId, @Nullable Long parentId, 
						 @Nullable Long forkedFromId) {
		super(id);
		this.name = name;
		this.key = key;
		this.path = path;
		this.serviceDeskEmailAddress = serviceDeskEmailAddress;
		this.codeManagement = codeManagement;
		this.issueManagement = issueManagement;
		this.gitPackConfig = gitPackConfig;
		this.lastEventDateId = lastEventDateId;
		this.parentId = parentId;	
		this.forkedFromId = forkedFromId;
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
	public Long getParentId() {
		return parentId;
	}

	@Nullable
	public Long getForkedFromId() {
		return forkedFromId;
	}

}
