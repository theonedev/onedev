package io.onedev.server.util.facade;

import javax.annotation.Nullable;

public class ProjectFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;
	
	private final String name;
	
	private final String serviceDeskName;
	
	private final Long parentId;
	
	public ProjectFacade(Long id, String name, @Nullable String serviceDeskName, @Nullable Long parentId) {
		super(id);
		this.name = name;
		this.serviceDeskName = serviceDeskName;
		this.parentId = parentId;
	}

	public String getName() {
		return name;
	}

	@Nullable
	public String getServiceDeskName() {
		return serviceDeskName;
	}

	@Nullable
	public Long getParentId() {
		return parentId;
	}

}
