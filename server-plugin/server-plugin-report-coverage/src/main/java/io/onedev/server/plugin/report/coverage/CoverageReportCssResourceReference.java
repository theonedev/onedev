package io.onedev.server.plugin.report.coverage;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class CoverageReportCssResourceReference extends BaseDependentCssResourceReference {

	private static final long serialVersionUID = 1L;

	public CoverageReportCssResourceReference() {
		super(CoverageReportCssResourceReference.class, "coverage-report.css");
	}

}
