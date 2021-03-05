package org.server.plugin.report.checkstyle;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class CheckstyleReportCssResourceReference extends BaseDependentCssResourceReference {

	private static final long serialVersionUID = 1L;

	public CheckstyleReportCssResourceReference() {
		super(CheckstyleReportCssResourceReference.class, "checkstyle-report.css");
	}

}
