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
				String startPage = LockUtils.read(page.getBuild().getReportCategoryLockKey(JobMarkdownReport.DIR), new Callable<String>() {

					@Override
					public String call() throws Exception {
						File startPageFile = new File(page.getBuild().getReportCategoryDir(JobMarkdownReport.DIR), 
								getTitle() + "/" + JobMarkdownReport.START_PAGE);
						return FileUtils.readFileToString(startPageFile, StandardCharsets.UTF_8);
					}
					
				});
				
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

}
