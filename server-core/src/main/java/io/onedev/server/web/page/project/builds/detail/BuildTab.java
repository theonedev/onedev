package io.onedev.server.web.page.project.builds.detail;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;

import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.PageTabLink;

@SuppressWarnings("serial")
public class BuildTab extends PageTab {

	public BuildTab(String title, Class<? extends BuildDetailPage> pageClass) {
		super(Model.of(title), pageClass);
	}
	
	@Override
	public Component render(String componentId) {
		return new PageTabLink(componentId, this) {

			@Override
			protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
				BuildDetailPage page = (BuildDetailPage) getPage();
				return new ViewStateAwarePageLink<Void>(linkId, pageClass, 
						BuildDetailPage.paramsOf(page.getBuild(), page.getCursor()));
			}
			
		};
	}
	
}