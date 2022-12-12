package io.onedev.server.plugin.report.checkstyle;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.buildspec.step.StepGroup;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.Patterns;

@Editable(order=3000, group=StepGroup.PUBLISH_REPORTS, name="ESLint")
public class PublishESLintReportStep extends PublishCheckstyleReportStep {

	private static final long serialVersionUID = 1L;
	
	@Editable(order=100, description="Specify ESLint report file in checkstyle format under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. "
			+ "This file can be generated with ESLint option <tt>'-f checkstyle'</tt> and <tt>'-o'</tt>. "
			+ "Use * or ? for pattern match")
	@Interpolative(variableSuggester="suggestVariables")
	@Patterns(path=true)
	@NotEmpty
	@Override
	public String getFilePatterns() {
		return super.getFilePatterns();
	}

	@Override
	public void setFilePatterns(String filePatterns) {
		super.setFilePatterns(filePatterns);
	}

}
