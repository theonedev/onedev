package io.onedev.server.web.component.tabbable;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public class ActionTabHead extends Panel {

	public ActionTabHead(String id, ActionTab tab) {
		super(id);
		
		WebMarkupContainer link = newLink("link", tab);
		add(link);
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
