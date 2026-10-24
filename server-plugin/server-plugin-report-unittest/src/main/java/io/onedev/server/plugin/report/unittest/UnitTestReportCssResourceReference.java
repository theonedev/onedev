package io.onedev.server.plugin.report.unittest;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class UnitTestReportCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public UnitTestReportCssResourceReference() {
		super(UnitTestReportCssResourceReference.class, "unit-test-report.css");
	}

}
