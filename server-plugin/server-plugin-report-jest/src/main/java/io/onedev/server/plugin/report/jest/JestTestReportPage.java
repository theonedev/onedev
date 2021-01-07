package io.onedev.server.plugin.report.jest;

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
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.utils.LockUtils;
import io.onedev.server.model.Build;
import io.onedev.server.model.support.BuildMetric;
import io.onedev.server.search.buildmetric.BuildMetricQuery;
import io.onedev.server.search.buildmetric.BuildMetricQueryParser;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.tabbable.PageTabHead;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.page.project.builds.detail.BuildTab;

@SuppressWarnings("serial")
public abstract class JestTestReportPage extends BuildDetailPage {

	private static final String PARAM_REPORT = "report";

	private final String reportName;
	
	private final IModel<JestTestReportData> reportDataModel = new LoadableDetachableModel<JestTestReportData>() {

		@Override
		protected JestTestReportData load() {
			return LockUtils.read(getBuild().getReportLockKey(JobJestReport.DIR), new Callable<JestTestReportData>() {

				@Override
				public JestTestReportData call() throws Exception {
					File reportsDir = new File(getBuild().getReportDir(JobJestReport.DIR), reportName);				
					return JestTestReportData.readFrom(reportsDir);
				}
				
			});
		}
		
	};
	
	public JestTestReportPage(PageParameters params) {
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
	protected JestTestReportData getReportData() {
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
		response.render(CssHeaderItem.forReference(new JestTestReportCssResourceReference()));
	}

	private class JestReportTab extends BuildTab {

		public JestReportTab(String title, Class<? extends JestTestReportPage> pageClass) {
			super(title, pageClass);
		}
		
		@Override
		protected Component renderOptions(String componentId) {
			Fragment fragment = new Fragment(componentId, "trendingFrag", JestTestReportPage.this);

			String query = String.format("%s \"last month\" and \"%s\" %s \"%s\"", 
					BuildMetricQuery.getRuleName(BuildMetricQueryParser.Since), 
					BuildMetric.NAME_REPORT, 
					BuildMetricQuery.getRuleName(BuildMetricQueryParser.Is), 
					reportName);
			PageParameters params = JestTestStatsPage.paramsOf(getProject(), query);
			fragment.add(new ViewStateAwarePageLink<>("link", JestTestStatsPage.class, params));
			return fragment;
		}

		@Override
		public Component render(String componentId) {
			return new PageTabHead(componentId, this) {

				@Override
				protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
					BuildDetailPage page = (BuildDetailPage) getPage();
					PageParameters params = JestTestReportPage.paramsOf(
							page.getBuild(), reportName);
					return new ViewStateAwarePageLink<Void>(linkId, pageClass, params);
				}
				
			};
		}

		@Override
		public boolean isActive(Page currentPage) {
			if (super.isActive(currentPage)) {
				JestTestReportPage jestReportPage = (JestTestReportPage) currentPage;
				return reportName.equals(jestReportPage.getReportName());
			} else {
				return false;
			}
		}
		
	}
	
}
