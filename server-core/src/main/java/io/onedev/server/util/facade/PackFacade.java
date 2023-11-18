package io.onedev.server.util.facade;

public class PackFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;
	
	private final Long projectId;
	
	private final String type;
	
	private final String version;
	
	public PackFacade(Long id, Long projectId, String type, String version) {
		super(id);
		this.projectId = projectId;
		this.type = type;
		this.version = version;
	}

	public Long getProjectId() {
		return projectId;
	}

	public String getType() {
		return type;
	}

	public String getVersion() {
		return version;
	}
}
