package io.onedev.server.buildspec.job;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.model.Build;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.validation.annotation.PathSegment;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.Patterns;

@Editable
public abstract class JobReport implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String reportName;
	
	private String filePatterns;
	
	private transient PatternSet patternSet;

	@Editable(order=50, description="Specify report name")
	@PathSegment
	@NotEmpty
	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}
	
	@Editable(order=100, description="Specify files relative to OneDev workspace. Use * or ? for pattern match")
	@Interpolative(variableSuggester="suggestVariables")
	@Patterns(path=true)
	@NotEmpty
	public String getFilePatterns() {
		return filePatterns;
	}

	public void setFilePatterns(String filePatterns) {
		this.filePatterns = filePatterns;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return Job.suggestVariables(matchWith);
	}

	public PatternSet getPatternSet() {
		if (patternSet == null)
			patternSet = PatternSet.parse(getFilePatterns());
		return patternSet;
	}
	
	public abstract void process(Build build, File workspace, SimpleLogger logger);

}
