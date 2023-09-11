package io.onedev.server.plugin.report.html;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

public class HtmlReportDownloadResourceReference extends ResourceReference {

	private static final long serialVersionUID = 1L;

	public HtmlReportDownloadResourceReference() {
		super("html-report");
	}

	@Override
	public IResource getResource() {
		return new HtmlReportDownloadResource();
	}

}
