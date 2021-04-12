package io.onedev.server.plugin.report.checkstyle;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.Patterns;

@Editable(name="ESLint Report")
public class JobESLintReport extends JobCheckstyleReport {

	private static final long serialVersionUID = 1L;
	
	@Editable(order=100, description="Specify ESLint report file in checkstyle format relative to repository root. "
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
