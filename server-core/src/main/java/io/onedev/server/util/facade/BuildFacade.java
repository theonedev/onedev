package io.onedev.server.util.facade;

public class BuildFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;
	
	private final Long projectId;

	private final Long number;
	
	private final String commitHash;
	
	public BuildFacade(Long id, Long projectId, Long number, String commitHash) {
		super(id);
		this.projectId = projectId;	
		this.number = number;
		this.commitHash = commitHash;
	}

	public Long getProjectId() {
		return projectId;
	}

	public Long getNumber() {
		return number;
	}

	public String getCommitHash() {	
		return commitHash;
	}
	
}
