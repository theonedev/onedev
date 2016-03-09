package com.pmease.commons.wicket.component.menu;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

@SuppressWarnings("serial")
abstract class MenuPanel extends Panel {
	
	public MenuPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		add(new ListView<MenuItem>("items", new LoadableDetachableModel<List<MenuItem>>() {

			@Override
			protected List<MenuItem> load() {
				return getMenuItems();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<MenuItem> item) {
				MenuItem menuItem  = item.getModelObject();
				if (menuItem != null) {
					Fragment fragment = new Fragment("content", "contentFrag", MenuPanel.this);
					AbstractLink link = menuItem.newLink("link");
					Component icon = new WebMarkupContainer("icon");
					String iconClass = menuItem.getIconClass();
					if (iconClass != null) {
						icon.add(AttributeAppender.append("class", iconClass + " fa fa-fw"));
					}
					link.add(icon);
					link.add(new Label("label", menuItem.getLabel()));
					fragment.add(link);
					item.add(fragment);
				} else {
					item.add(new Label("content", "<div></div>").setEscapeModelStrings(false));
					item.add(AttributeAppender.append("class", "divider"));
				}
			}
			
		});
	}
	
	protected abstract List<MenuItem> getMenuItems();

}
