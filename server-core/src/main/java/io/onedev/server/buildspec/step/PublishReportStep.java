package io.onedev.server.buildspec.step;

import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.ExecuteCondition;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.validation.annotation.PathSegment;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.Patterns;

@Editable
public abstract class PublishReportStep extends ServerSideStep {

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
	
	public PublishReportStep() {
		setCondition(ExecuteCondition.ALWAYS);
	}
	
	@Override
	protected PatternSet getFiles() {
		return PatternSet.parse(getFilePatterns());
	}
	
	@Editable(order=100, description="Specify files under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> to be published. "
			+ "Use * or ? for pattern match")
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
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}

	public PatternSet getPatternSet() {
		if (patternSet == null)
			patternSet = PatternSet.parse(getFilePatterns());
		return patternSet;
	}
	
}
