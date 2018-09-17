package io.onedev.server.util.facade;

public class BuildFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;
	
	private final Long configurationId;
	
	private final String commitHash;
	
	public BuildFacade(Long buildId, Long configurationId, String commitHash) {
		super(buildId);
		this.configurationId = configurationId;
		this.commitHash = commitHash;
	}

	public Long getConfigurationId() {
		return configurationId;
	}

	public String getCommitHash() {
		return commitHash;
	}

}
