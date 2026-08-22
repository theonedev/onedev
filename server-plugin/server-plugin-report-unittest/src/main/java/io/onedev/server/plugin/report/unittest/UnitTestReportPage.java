package io.onedev.server.plugin.report.unittest;

import io.onedev.server.codequality.UnitTestReport;
import io.onedev.server.exception.ExceptionUtils;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.tabbable.PageTabHead;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.page.project.builds.detail.BuildTab;
import io.onedev.server.web.page.project.builds.detail.report.BuildReportPage;
import org.apache.commons.lang3.SerializationException;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.jspecify.annotations.Nullable;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.List;

public abstract class UnitTestReportPage extends BuildReportPage {

	private final IModel<UnitTestReport> reportModel = new LoadableDetachableModel<>() {

		@Override
		protected UnitTestReport load() {
			try {
				return UnitTestReport.readFrom(getBuild(), getReportName());
			} catch (Exception e) {
				if (ExceptionUtils.find(e, SerializationException.class) != null)
					return null;
				else 
					throw ExceptionUtils.unchecked(e);
			}
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
		tabs.add(new UnitReportTab(_T("Test Suites"), UnitTestSuitesPage.class));
		tabs.add(new UnitReportTab(_T("Test Cases"), UnitTestCasesPage.class));
		
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
			super(Model.of(title), pageClass, new PageParameters());
		}

		@Override
		public Component render(String componentId) {
			return new PageTabHead(componentId, this) {

				@Override
				protected Link<?> newLink(String linkId, Class<? extends Page> pageClass, PageParameters pageParams) {
					BuildDetailPage page = (BuildDetailPage) getPage();
					PageParameters params = UnitTestReportPage.paramsOf(page.getBuild(), getReportName());
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
	
}
