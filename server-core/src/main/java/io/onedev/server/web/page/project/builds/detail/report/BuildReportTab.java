package io.onedev.server.web.page.project.builds.detail.report;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.Link;

import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.tabbable.PageTabHead;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.page.project.builds.detail.BuildTab;
import io.onedev.server.web.page.project.stats.buildmetric.BuildMetricStatsPage;

public class BuildReportTab extends BuildTab {

	private static final long serialVersionUID = 1L;

	private final Class<? extends BuildMetricStatsPage<?>> statsPageClass;
	
	public BuildReportTab(String title, Class<? extends BuildReportPage> mainPageClass, 
			@Nullable Class<? extends BuildMetricStatsPage<?>> statsPageClass) {
		super(title, mainPageClass);
		this.statsPageClass = statsPageClass;
	}
	
	public BuildReportTab(String title, Class<? extends BuildReportPage> mainPageClass, 
			Class<? extends BuildReportPage> additionalPageClass1, 
			@Nullable Class<? extends BuildMetricStatsPage<?>> statsPageClass) {
		super(title, mainPageClass, additionalPageClass1);
		this.statsPageClass = statsPageClass;
	}
	
	public BuildReportTab(String title, Class<? extends BuildReportPage> mainPageClass, 
			Class<? extends BuildReportPage> additionalPageClass1, 
			Class<? extends BuildReportPage> additionalPageClass2, 
			@Nullable Class<? extends BuildMetricStatsPage<?>> statsPageClass) {
		super(title, mainPageClass, additionalPageClass1, additionalPageClass2);
		this.statsPageClass = statsPageClass;
	}
	
	public BuildReportTab(String title, Class<? extends BuildReportPage> mainPageClass, 
			Class<? extends BuildReportPage> additionalPageClass1, 
			Class<? extends BuildReportPage> additionalPageClass2, 
			Class<? extends BuildReportPage> additionalPageClass3, 
			@Nullable Class<? extends BuildMetricStatsPage<?>> statsPageClass) {
		super(title, mainPageClass, additionalPageClass1, additionalPageClass2, additionalPageClass3);
		this.statsPageClass = statsPageClass;
	}

	@Override
	public Component render(String componentId) {
		return new PageTabHead(componentId, this) {

			private static final long serialVersionUID = 1L;

			@Override
			protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
				BuildDetailPage page = (BuildDetailPage) getPage();
				return new ViewStateAwarePageLink<Void>(linkId, pageClass, 
						BuildReportPage.paramsOf(page.getBuild(), getTitle()));
			}
			
		};
	}

	@Override
	protected Component renderOptions(String componentId) {
		if (statsPageClass != null)
			return new BuildReportTabOptionPanel(componentId, statsPageClass, getTitle());
		else
			return super.renderOptions(componentId);
	}

	@Override
	public boolean isActive(Page currentPage) {
		if (super.isActive(currentPage)) {
			BuildReportPage buildReportPage = (BuildReportPage) currentPage;
			return getTitle().equals(buildReportPage.getReportName());
		} else {
			return false;
		}
	}

}