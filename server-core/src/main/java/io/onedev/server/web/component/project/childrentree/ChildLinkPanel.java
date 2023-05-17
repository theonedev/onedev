package io.onedev.server.web.component.project.childrentree;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.web.component.project.ProjectAvatar;

@SuppressWarnings("serial")
abstract class ChildLinkPanel extends Panel {

	private final ProjectFacade child;
	
	public ChildLinkPanel(String componentId, ProjectFacade child) {
		super(componentId);
		this.child = child;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		WebMarkupContainer link = newChildLink("link", child.getId());		
		link.add(new ProjectAvatar("avatar", child.getId()));
		link.add(new Label("name", child.getName()));
		add(link);
	}
	
	protected abstract WebMarkupContainer newChildLink(String componentId, Long childId);

}
