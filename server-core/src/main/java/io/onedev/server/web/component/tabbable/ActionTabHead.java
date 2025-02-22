package io.onedev.server.web.component.tabbable;

import io.onedev.server.web.component.svg.SpriteImage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

public class ActionTabHead extends Panel {

	public ActionTabHead(String id, ActionTab tab) {
		super(id);
		
		WebMarkupContainer link = newLink("link", tab);
		add(link);
		if (tab.getIconModel() != null)
			link.add(new SpriteImage("icon", tab.getIconModel()));
		else
			link.add(new WebMarkupContainer("icon").setVisible(false));
		link.add(new Label("label", tab.getTitleModel()));
	}
	
	protected WebMarkupContainer newLink(String id, final ActionTab tab) {
		return new Link<Void>(id) {

			@Override
			public void onClick() {
				tab.selectTab(this);
			}
			
		};
	}

}
