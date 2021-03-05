package io.onedev.server.web.page.project.builds.detail.report;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.Link;

import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.tabbable.PageTabHead;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.page.project.builds.detail.BuildTab;

public class BuildReportTab extends BuildTab {

	private static final long serialVersionUID = 1L;

	public BuildReportTab(String title, Class<? extends BuildReportPage> mainPageClass) {
		super(title, mainPageClass);
	}
	
	public BuildReportTab(String title, Class<? extends BuildReportPage> mainPageClass, 
			Class<? extends BuildReportPage> additionalPageClass1) {
		super(title, mainPageClass, additionalPageClass1);
	}
	
	public BuildReportTab(String title, Class<? extends BuildReportPage> mainPageClass, 
			Class<? extends BuildReportPage> additionalPageClass1, 
			Class<? extends BuildReportPage> additionalPageClass2) {
		super(title, mainPageClass, additionalPageClass1, additionalPageClass2);
	}
	
	public BuildReportTab(String title, Class<? extends BuildReportPage> mainPageClass, 
			Class<? extends BuildReportPage> additionalPageClass1, 
			Class<? extends BuildReportPage> additionalPageClass2, 
			Class<? extends BuildReportPage> additionalPageClass3) {
		super(title, mainPageClass, additionalPageClass1, additionalPageClass2, additionalPageClass3);
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
	public boolean isActive(Page currentPage) {
		if (super.isActive(currentPage)) {
			BuildReportPage buildReportPage = (BuildReportPage) currentPage;
			return getTitle().equals(buildReportPage.getReportName());
		} else {
			return false;
		}
	}

}