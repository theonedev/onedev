package io.onedev.server.plugin.report.markdown;

import java.io.File;
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

import com.google.common.base.Splitter;

import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Build;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;

@SuppressWarnings("serial")
public class MarkdownReportPage extends BuildDetailPage {

	private static final String PARAM_REPORT = "report";
	
	private final String reportName;
	
	private final String filePath;
	
	public MarkdownReportPage(PageParameters params) {
		super(params);
		
		reportName = params.get(PARAM_REPORT).toString();
		
		List<String> pathSegments = new ArrayList<>();
		for (int i=0; i<params.getIndexedCount(); i++) {
			String segment = params.get(i).toString();
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

		Long projectId = getBuild().getProject().getId();
		ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
		String markdown = projectManager.runOnActiveServer(projectId, new GetMarkdownContent(projectId, getBuild().getNumber(), reportName, filePath));
		add(new MarkdownViewer("markdownReport", Model.of(markdown), null));
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
		int index = 0;
		if (filePath != null) {
			for (String segment: Splitter.on("/").split(filePath)) {
				params.set(index, segment);
				index++;
			}
		}
		return params;
	}
	
	private static class GetMarkdownContent implements ClusterTask<String> {

		private final Long projectId;
		
		private final Long buildNumber;
		
		private final String reportName;
		
		private final String filePath;
		
		private GetMarkdownContent(Long projectId, Long buildNumber, String reportName, String filePath) {
			this.projectId = projectId;
			this.buildNumber = buildNumber;
			this.reportName = reportName;
			this.filePath = filePath;
		}
		
		@Override
		public String call() throws Exception {
			File file = new File(Build.getStorageDir(projectId, buildNumber), 
					PublishMarkdownReportStep.CATEGORY + "/" + reportName + "/" + filePath);
			return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
		}
		
	}
}
