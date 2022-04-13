package io.onedev.server.web.component.project.childrentree;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.project.avatar.ProjectAvatar;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;

@SuppressWarnings("serial")
class ChildLinkPanel extends Panel {

	private final ProjectFacade child;
	
	public ChildLinkPanel(String componentId, ProjectFacade child) {
		super(componentId);
		this.child = child;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		Link<?> link = new ViewStateAwarePageLink<Void>("link", ProjectDashboardPage.class, 
				ProjectDashboardPage.paramsOf(child.getId()));		
		link.add(new ProjectAvatar("avatar", child.getId()));
		link.add(new Label("name", child.getName()));
		add(link);
	}

}
