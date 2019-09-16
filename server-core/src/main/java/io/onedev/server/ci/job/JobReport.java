package io.onedev.server.ci.job;

import java.io.File;
import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.model.Build;
import io.onedev.server.util.JobLogger;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Patterns;

@Editable
public abstract class JobReport implements Serializable {

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
	
}
