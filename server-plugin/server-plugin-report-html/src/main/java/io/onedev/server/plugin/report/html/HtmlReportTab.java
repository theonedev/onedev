package io.onedev.server.plugin.report.html;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.tabbable.PageTabLink;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.page.project.builds.detail.BuildTab;

@SuppressWarnings("serial")
public class HtmlReportTab extends BuildTab {

	public HtmlReportTab(String reportName) {
		super(reportName, HtmlReportPage.class);
	}

	@Override
	public Component render(String componentId) {
		return new PageTabLink(componentId, this) {

			@Override
			protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
				BuildDetailPage page = (BuildDetailPage) getPage();
				PageParameters params = HtmlReportPage.paramsOf(
						page.getBuild(), page.getCursor(), getTitle());
				return new ViewStateAwarePageLink<Void>(linkId, pageClass, params);
			}
			
		};
	}

	@Override
	public boolean isActive(Page currentPage) {
		if (super.isActive(currentPage)) {
			HtmlReportPage htmlReportPage = (HtmlReportPage) currentPage;
			return getTitle().equals(htmlReportPage.getReportName());
		} else {
			return false;
		}
	}

}
