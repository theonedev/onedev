package io.onedev.server.plugin.report.markdown;

import java.io.Serial;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

public class MarkdownReportDownloadResourceReference extends ResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public MarkdownReportDownloadResourceReference() {
		super("markdown-report");
	}

	@Override
	public IResource getResource() {
		return new MarkdownReportDownloadResource();
	}

}
