package io.onedev.server.plugin.report.problem;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class ProblemReportCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public ProblemReportCssResourceReference() {
		super(ProblemReportCssResourceReference.class, "problem-report.css");
	}

}
