package io.onedev.server.plugin.report.jest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.utils.LockUtils;
import io.onedev.server.model.Build;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.tabbable.PageTabHead;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.page.project.builds.detail.BuildTab;

@SuppressWarnings("serial")
public abstract class JestReportPage extends BuildDetailPage {

	private static final String PARAM_REPORT = "report";

	private final String reportName;
	
	private final IModel<JestReportData> reportDataModel = new LoadableDetachableModel<JestReportData>() {

		@Override
		protected JestReportData load() {
			return LockUtils.read(getBuild().getReportLockKey(JobJestReport.DIR), new Callable<JestReportData>() {

				@Override
				public JestReportData call() throws Exception {
					File reportsDir = new File(getBuild().getReportDir(JobJestReport.DIR), reportName);				
					return JestReportData.readFrom(reportsDir);
				}
				
			});
		}
		
	};
	
	public JestReportPage(PageParameters params) {
		super(params);
		
		reportName = params.get(PARAM_REPORT).toString();
	}
	
	@Override
	protected void onDetach() {
		reportDataModel.detach();
		super.onDetach();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<Tab> tabs = new ArrayList<>();
		tabs.add(new JestReportTab("Test Suites", JestTestSuitesPage.class));
		tabs.add(new JestReportTab("Test Cases", JestTestCasesPage.class));
		
		add(new Tabbable("reportTabs", tabs));
	}
	
	@Nullable
	protected JestReportData getReportData() {
		return reportDataModel.getObject();
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canAccessReport(getBuild(), reportName);
	}
	
	public String getReportName() {
		return reportName;
	}

	public static PageParameters paramsOf(Build build, String reportName) {
		PageParameters params = paramsOf(build);
		params.add(PARAM_REPORT, reportName);
		return params;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new JestReportResourceReference()));
	}

	private class JestReportTab extends BuildTab {

		public JestReportTab(String title, Class<? extends JestReportPage> pageClass) {
			super(title, pageClass);
		}
		
		@Override
		public Component render(String componentId) {
			return new PageTabHead(componentId, this) {

				@Override
				protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
					BuildDetailPage page = (BuildDetailPage) getPage();
					PageParameters params = JestReportPage.paramsOf(
							page.getBuild(), reportName);
					return new ViewStateAwarePageLink<Void>(linkId, pageClass, params);
				}
				
			};
		}

		@Override
		public boolean isActive(Page currentPage) {
			if (super.isActive(currentPage)) {
				JestReportPage jestReportPage = (JestReportPage) currentPage;
				return reportName.equals(jestReportPage.getReportName());
			} else {
				return false;
			}
		}
		
	}
	
}
