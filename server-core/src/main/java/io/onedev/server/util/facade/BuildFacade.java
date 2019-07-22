package io.onedev.server.util.facade;

public class BuildFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;
	
	private final Long projectId;
	
	private final String commitHash;
	
	public BuildFacade(Long buildId, Long projectId, String commitHash) {
		super(buildId);
		this.projectId = projectId;
		this.commitHash = commitHash;
	}

	public Long getProjectId() {
		return projectId;
	}

	public String getCommitHash() {
		return commitHash;
	}

}
