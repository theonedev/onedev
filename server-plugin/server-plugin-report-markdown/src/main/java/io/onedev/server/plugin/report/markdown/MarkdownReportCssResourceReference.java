package io.onedev.server.plugin.report.markdown;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class MarkdownReportCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public MarkdownReportCssResourceReference() {
		super(MarkdownReportCssResourceReference.class, "markdown-report.css");
	}

}
