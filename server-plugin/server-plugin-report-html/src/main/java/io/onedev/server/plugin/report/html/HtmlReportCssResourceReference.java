package io.onedev.server.plugin.report.html;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class HtmlReportCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public HtmlReportCssResourceReference() {
		super(HtmlReportCssResourceReference.class, "html-report.css");
	}

}
