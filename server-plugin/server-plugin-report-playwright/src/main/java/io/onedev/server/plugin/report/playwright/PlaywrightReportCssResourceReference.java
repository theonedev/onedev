package io.onedev.server.plugin.report.playwright;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class PlaywrightReportCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public PlaywrightReportCssResourceReference() {
		super(PlaywrightReportCssResourceReference.class, "playwright-report.css");
	}

}
