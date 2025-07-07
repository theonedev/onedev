package io.onedev.server.web.component.project.childrentree;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.web.component.project.ProjectAvatar;

abstract class ChildLinkPanel extends Panel {

	private final ProjectFacade child;
	
	public ChildLinkPanel(String componentId, ProjectFacade child) {
		super(componentId);
		this.child = child;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		if (child.getId() != null) {
			WebMarkupContainer link = newChildLink("link", child.getId());		
			link.add(new ProjectAvatar("avatar", child.getId()));
			link.add(new Label("name", child.getName()));
			add(link);
		} else {
			var link = new WebMarkupContainer("link") {
				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					tag.setName("span");
				}
			};
			link.add(new WebMarkupContainer("avatar").setVisible(false));
			link.add(new Label("name", "<span class='text-warning'>Too many projects to display</span>").setEscapeModelStrings(false));
			add(link);
		}
	}
	
	protected abstract WebMarkupContainer newChildLink(String componentId, Long childId);

}
