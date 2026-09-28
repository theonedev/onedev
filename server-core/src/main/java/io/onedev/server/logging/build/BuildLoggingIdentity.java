package io.onedev.server.logging.build;

import static io.onedev.k8shelper.JobHelper.FINALIZATION;
import static io.onedev.k8shelper.JobHelper.INITIALIZATION;

import java.io.File;
import java.util.Objects;

import io.onedev.k8shelper.JobHelper;
import io.onedev.server.logging.LoggingIdentity;
import io.onedev.server.model.Build;

/** Identifies the log file of one named build stage. */
public class BuildLoggingIdentity implements LoggingIdentity {

	private static final long serialVersionUID = 1L;

	private final Long projectId;

	private final Long buildNumber;

	private final String stage;
	
	public BuildLoggingIdentity(Long projectId, Long buildNumber, String stage) {
		this.projectId = projectId;
		this.buildNumber = buildNumber;
		this.stage = Objects.requireNonNull(stage);
	}

	@Override
	public File getFile() {
		var directory = Build.getLogDir(projectId, buildNumber);
		return new File(directory, getFileName(stage));
	}

	public static String getFileName(String stage) {
		if (stage.equals(INITIALIZATION) || stage.equals(FINALIZATION))
			return stage + ".log";
		if (!JobHelper.STEP_ID_PATTERN.matcher(stage).matches())
			throw new IllegalArgumentException("Invalid build stage: " + stage);
		JobHelper.parseStepPosition(stage.substring("step-".length()));
		return stage + ".log";
	}

	@Override
	public String getLockName() {
		return Build.getLogLockName(projectId, buildNumber);
	}

}
