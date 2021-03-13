package io.onedev.server.plugin.report.markdown;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.Build;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;

@SuppressWarnings("serial")
public class MarkdownReportPage extends BuildDetailPage {

	private static final String PARAM_REPORT = "report";
	
	private static final String PARAM_PATH = "path";
	
	private final String reportName;
	
	private final String filePath;
	
	public MarkdownReportPage(PageParameters params) {
		super(params);
		
		reportName = params.get(PARAM_REPORT).toString();
		
		List<String> pathSegments = new ArrayList<>();
		String segment = params.get(PARAM_PATH).toString();
		if (segment != null && segment.length() != 0)
			pathSegments.add(segment);
		
		for (int i=0; i<params.getIndexedCount(); i++) {
			segment = params.get(i).toString();
			if (segment.length() != 0)
				pathSegments.add(segment);
		}
		
		filePath = StringUtils.join(pathSegments, "/");
		if (!filePath.endsWith(".md")) {
			RequestCycle.get().scheduleRequestHandlerAfterCurrent(
					new ResourceReferenceRequestHandler(new MarkdownReportDownloadResourceReference(), getPageParameters()));
		}
		
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		File file = new File(getBuild().getReportCategoryDir(JobMarkdownReport.DIR), 
				reportName + "/" + filePath);
		try {
			String markdown = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
			add(new MarkdownViewer("markdownReport", Model.of(markdown), null));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canAccessReport(getBuild(), reportName);
	}
	
	public String getReportName() {
		return reportName;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new MarkdownReportCssResourceReference()));
	}

	public static PageParameters paramsOf(Build build, String reportName, @Nullable String filePath) {
		PageParameters params = paramsOf(build);
		params.add(PARAM_REPORT, reportName);
		if (filePath != null)
			params.add(PARAM_PATH, filePath);
		return params;
	}
}
