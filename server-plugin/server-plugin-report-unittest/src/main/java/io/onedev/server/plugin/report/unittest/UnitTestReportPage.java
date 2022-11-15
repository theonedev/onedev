package io.onedev.server.plugin.report.unittest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Build;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.tabbable.PageTabHead;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.page.project.builds.detail.BuildTab;
import io.onedev.server.web.page.project.builds.detail.report.BuildReportPage;

@SuppressWarnings("serial")
public abstract class UnitTestReportPage extends BuildReportPage {

	private final IModel<UnitTestReport> reportModel = new LoadableDetachableModel<>() {

		@Override
		protected UnitTestReport load() {
			Long projectId = getProject().getId();
			Long buildNumber = getBuild().getNumber();
			return OneDev.getInstance(ProjectManager.class).runOnProjectServer(projectId, new GetUnitTestReport(projectId, buildNumber, getReportName()));
		}
		
	};
	
	public UnitTestReportPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected void onDetach() {
		reportModel.detach();
		super.onDetach();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<Tab> tabs = new ArrayList<>();
		tabs.add(new UnitReportTab("Test Suites", UnitTestSuitesPage.class));
		tabs.add(new UnitReportTab("Test Cases", UnitTestCasesPage.class));
		
		add(new Tabbable("reportTabs", tabs));
	}
	
	@Nullable
	protected UnitTestReport getReport() {
		return reportModel.getObject();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new UnitTestReportCssResourceReference()));
	}

	private class UnitReportTab extends BuildTab {

		public UnitReportTab(String title, Class<? extends UnitTestReportPage> pageClass) {
			super(title, pageClass);
		}

		@Override
		public Component render(String componentId) {
			return new PageTabHead(componentId, this) {

				@Override
				protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
					BuildDetailPage page = (BuildDetailPage) getPage();
					PageParameters params = UnitTestReportPage.paramsOf(
							page.getBuild(), getReportName());
					return new ViewStateAwarePageLink<Void>(linkId, pageClass, params);
				}
				
			};
		}

		@Override
		public boolean isActive(Page currentPage) {
			if (super.isActive(currentPage)) {
				UnitTestReportPage unitTestReportPage = (UnitTestReportPage) currentPage;
				return getReportName().equals(unitTestReportPage.getReportName());
			} else {
				return false;
			}
		}
		
	}
	
	private static class GetUnitTestReport implements ClusterTask<UnitTestReport> {

		private final Long projectId;
		
		private final Long buildNumber;
		
		private final String reportName;
		
		private GetUnitTestReport(Long projectId, Long buildNumber, String reportName) {
			this.projectId = projectId;
			this.buildNumber = buildNumber;
			this.reportName = reportName;
		}		
		
		@Override
		public UnitTestReport call() throws Exception {
			return LockUtils.read(UnitTestReport.getReportLockName(projectId, buildNumber), new Callable<UnitTestReport>() {

				@Override
				public UnitTestReport call() throws Exception {
					File reportDir = new File(Build.getDir(projectId, buildNumber), UnitTestReport.CATEGORY + "/" + reportName);				
					return UnitTestReport.readFrom(reportDir);
				}
				
			});
		}
		
	}
}
