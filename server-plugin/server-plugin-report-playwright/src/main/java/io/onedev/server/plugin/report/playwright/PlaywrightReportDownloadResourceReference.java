package io.onedev.server.plugin.report.playwright;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

public class PlaywrightReportDownloadResourceReference extends ResourceReference {

	private static final long serialVersionUID = 1L;

	public PlaywrightReportDownloadResourceReference() {
		super("playwright-report");
	}

	@Override
	public IResource getResource() {
		return new PlaywrightReportDownloadResource();
	}

}
