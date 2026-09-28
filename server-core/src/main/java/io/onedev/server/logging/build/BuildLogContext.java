package io.onedev.server.logging.build;

import java.io.Serializable;

import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.model.Build;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.ProjectService;

/** Context for whole-build log aggregation and lifecycle operations. */
public class BuildLogContext implements Serializable {

	private static final long serialVersionUID = 1L;

	private final BuildLogStorage storage;

	private final Long buildId;

	private final Long projectId;

	private final Long buildNumber;

	private final String token;

	public BuildLogContext(Build build) {
		buildId = build.getId();
		projectId = build.getProject().getId();
		buildNumber = build.getNumber();
		token = build.getToken();
		storage = new BuildLogStorage(projectId, buildNumber);
	}

	Long getProjectId() {
		return projectId;
	}

	Long getBuildNumber() {
		return buildNumber;
	}

	String getToken() {
		return token;
	}

	public Long getBuildId() {
		return buildId;
	}

	public <T> T runOnActiveServer(ClusterTask<T> task) {
		return OneDev.getInstance(ProjectService.class).runOnActiveServer(projectId, task);
	}

	public void fileModified() {
		OneDev.getInstance(ProjectService.class).directoryModified(projectId, storage.getDirectory());
	}

	String getLogVersion() {
		return OneDev.getInstance(SessionService.class).call(() -> {
			var build = OneDev.getInstance(BuildService.class).load(buildId);
			return build.getToken() + ":" + (build.getRetryDate() != null ? build.getRetryDate().getTime() : 0);
		});
	}

	public BuildLogStorage getStorage() {
		return storage;
	}

}
