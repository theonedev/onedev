package io.onedev.server.util.facade;

public class ProjectBelongingFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;
	
	private final Long projectId;
	
	public ProjectBelongingFacade(Long id, Long projectId) {
		super(id);
		this.projectId = projectId;
	}

	public Long getProjectId() {
		return projectId;
	}

}
