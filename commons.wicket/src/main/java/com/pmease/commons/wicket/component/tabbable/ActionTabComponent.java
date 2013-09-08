package com.pmease.commons.wicket.component.tabbable;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public class ActionTabComponent extends Panel {

	public ActionTabComponent(String id, ActionTab tab) {
		super(id);
		
		Link<?> link = newLink("link", tab);
		add(link);
		link.add(new Label("label", tab.getTitleModel()));
	}

	protected Link<?> newLink(String id, final ActionTab tab) {
		return new Link<Void>("link") {

			@Override
			public void onClick() {
				@SuppressWarnings("unchecked")
				ListView<ActionTab> tabItems = findParent(ListView.class);
				for (ActionTab each: tabItems.getModelObject())
					each.setActive(false);
				
				tab.setActive(true);
				tab.tabActivated();
			}
			
		};
	}

}
