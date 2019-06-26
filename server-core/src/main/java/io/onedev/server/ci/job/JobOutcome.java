package io.onedev.server.ci.job;

import java.io.File;
import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.ci.job.log.JobLogger;
import io.onedev.server.model.Build;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Patterns;

@Editable
public abstract class JobOutcome implements Serializable {

	private static final long serialVersionUID = 1L;

	private String filePatterns;
	
	private transient PatternSet patternSet;

	@Editable(order=100, description="Specify files relative to OneDev workspace. Use * or ? for pattern match")
	@Patterns
	@NotEmpty
	public String getFilePatterns() {
		return filePatterns;
	}

	public void setFilePatterns(String filePatterns) {
		this.filePatterns = filePatterns;
	}

	public PatternSet getPatternSet() {
		if (patternSet == null)
			patternSet = PatternSet.fromString(getFilePatterns());
		return patternSet;
	}
	
	public abstract void process(Build build, File workspace, JobLogger logger);
	
	public static String getLockKey(Build build, String outcomeDir) {
		return "job-outcome:" + build.getId() + ":" + outcomeDir;
	}
	
	public static File getOutcomeDir(Build build, String outcomeDir) {
		File buildDir = OneDev.getInstance(StorageManager.class)
				.getBuildDir(build.getProject().getId(), build.getNumber());
		return new File(buildDir, outcomeDir);
	}
	
}
