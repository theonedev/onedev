package org.server.plugin.report.checkstyle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.utils.LockUtils;
import io.onedev.server.code.CodeProblem.Severity;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.component.tabbable.PageTabHead;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.page.project.builds.detail.BuildTab;
import io.onedev.server.web.page.project.builds.detail.report.BuildReportPage;

@SuppressWarnings("serial")
public class CheckstyleReportPage extends BuildReportPage {

	protected static final int MAX_VIOLATIONS_TO_DISPLAY = 200;
	
	private final IModel<CheckstyleReportData> reportDataModel = new LoadableDetachableModel<CheckstyleReportData>() {

		@Override
		protected CheckstyleReportData load() {
			return LockUtils.read(getBuild().getReportCategoryLockKey(JobCheckstyleReport.DIR), new Callable<CheckstyleReportData>() {

				@Override
				public CheckstyleReportData call() throws Exception {
					File reportsDir = new File(getBuild().getReportCategoryDir(JobCheckstyleReport.DIR), getReportName());				
					return CheckstyleReportData.readFrom(reportsDir);
				}
				
			});
		}
		
	};
	
	public CheckstyleReportPage(PageParameters params) {
		super(params);
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
		tabs.add(new CheckstyleReportTab("By Files", CheckstyleFilesPage.class));
		tabs.add(new CheckstyleReportTab("By Rules", CheckstyleRulesPage.class));
		
		add(new Tabbable("reportTabs", tabs));
	}
	
	@Nullable
	protected CheckstyleReportData getReportData() {
		return reportDataModel.getObject();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CheckstyleReportCssResourceReference()));
	}
	
	protected SpriteImage newSeverityIcon(String componentId, Severity severity) {
		String iconHref;
		String iconClass;
		switch (severity) {
		case ERROR:
			iconHref = "times-circle-o";
			iconClass = "text-danger";
			break;
		case WARNING:
			iconHref = "warning-o";
			iconClass = "text-warning";
			break;
		default:
			iconClass = "text-info";
			iconHref = "info-circle-o";
		}
		
		SpriteImage icon = new SpriteImage(componentId, iconHref);
		icon.add(AttributeAppender.append("class", iconClass));
		return icon;
	}
	
	private class CheckstyleReportTab extends BuildTab {

		public CheckstyleReportTab(String title, Class<? extends CheckstyleReportPage> pageClass) {
			super(title, pageClass);
		}
		
		@Override
		public Component render(String componentId) {
			return new PageTabHead(componentId, this) {

				@Override
				protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
					BuildDetailPage page = (BuildDetailPage) getPage();
					PageParameters params = CheckstyleReportPage.paramsOf(
							page.getBuild(), getReportName());
					return new ViewStateAwarePageLink<Void>(linkId, pageClass, params);
				}
				
			};
		}

		@Override
		public boolean isActive(Page currentPage) {
			if (super.isActive(currentPage)) {
				CheckstyleReportPage jestReportPage = (CheckstyleReportPage) currentPage;
				return getReportName().equals(jestReportPage.getReportName());
			} else {
				return false;
			}
		}
		
	}
	
}
