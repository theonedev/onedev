package io.onedev.server.util.facade;

public class BuildFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;
	
	private final Long projectId;
	
	private final String commitHash;
	
	private final String jobName;
	
	public BuildFacade(Long buildId, Long projectId, String commitHash, String jobName) {
		super(buildId);
		this.projectId = projectId;
		this.commitHash = commitHash;
		this.jobName = jobName;
	}

	public Long getProjectId() {
		return projectId;
	}

	public String getCommitHash() {
		return commitHash;
	}

	public String getJobName() {
		return jobName;
	}

}
