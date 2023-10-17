package io.onedev.server.plugin.report.markdown;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Build;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.tabbable.PageTabHead;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.page.project.builds.detail.BuildTab;

@SuppressWarnings("serial")
public class MarkdownReportTab extends BuildTab {

	public MarkdownReportTab(String reportName) {
		super(reportName, MarkdownReportPage.class);
	}

	@Override
	public Component render(String componentId) {
		return new PageTabHead(componentId, this) {

			@Override
			protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
				BuildDetailPage page = (BuildDetailPage) getPage();
				Build build = page.getBuild();
				Long projectId = build.getProject().getId();
				Long buildNumber = build.getNumber();
				ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
				String startPage = projectManager.runOnActiveServer(projectId, new GetStartPage(projectId, buildNumber, getTitle()));
				
				PageParameters params = MarkdownReportPage.paramsOf(page.getBuild(), getTitle(), startPage);
				return new ViewStateAwarePageLink<Void>(linkId, pageClass, params);
			}
			
		};
	}

	@Override
	public boolean isActive(Page currentPage) {
		if (super.isActive(currentPage)) {
			MarkdownReportPage markdownReportPage = (MarkdownReportPage) currentPage;
			return getTitle().equals(markdownReportPage.getReportName());
		} else {
			return false;
		}
	}

	private static class GetStartPage implements ClusterTask<String> {

		private Long projectId;
		
		private Long buildNumber;
		
		private String reportName;
		
		private GetStartPage(Long projectId, Long buildNumber, String reportName) {
			this.projectId = projectId;
			this.buildNumber = buildNumber;
			this.reportName = reportName;
		}
		
		@Override
		public String call() {
			return LockUtils.read(PublishMarkdownReportStep.getReportLockName(projectId, buildNumber), new Callable<String>() {

				@Override
				public String call() throws Exception {
					File startPageFile = new File(Build.getStorageDir(projectId, buildNumber), 
							PublishMarkdownReportStep.CATEGORY + "/" + reportName + "/" + PublishMarkdownReportStep.START_PAGE);
					return FileUtils.readFileToString(startPageFile, StandardCharsets.UTF_8);
				}
				
			});
		}
		
	}
}
