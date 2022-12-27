package io.onedev.server.util.facade;

import javax.annotation.Nullable;

public class ProjectFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;
	
	private final String name;
	
	private final String path;
	
	private final String serviceDeskName;
	
	private final boolean issueManagement;
	
	private final Long defaultRoleId;
	
	private final Long parentId;
	
	public ProjectFacade(Long id, String name, String path,
						 @Nullable String serviceDeskName, boolean issueManagement,
						 @Nullable Long defaultRoleId, @Nullable Long parentId) {
		super(id);
		this.name = name;
		this.path = path;
		this.serviceDeskName = serviceDeskName;
		this.issueManagement = issueManagement;
		this.defaultRoleId = defaultRoleId;
		this.parentId = parentId;
	}

	public String getName() {
		return name;
	}

	@Nullable
	public String getPath() {
		return path;
	}

	public boolean isIssueManagement() {
		return issueManagement;
	}

	@Nullable
	public String getServiceDeskName() {
		return serviceDeskName;
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
