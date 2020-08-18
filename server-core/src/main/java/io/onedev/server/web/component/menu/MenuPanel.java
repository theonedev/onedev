package io.onedev.server.web.component.menu;

import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.web.component.svg.SpriteImage;

@SuppressWarnings("serial")
abstract class MenuPanel extends Panel {
	
	public MenuPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);

		List<MenuItem> menuItems = getMenuItems();
		boolean hasIcons = false;
		boolean hasShortcuts = false;
		for (MenuItem menuItem: menuItems) {
			if (menuItem.getIconHref() != null)
				hasIcons = true;
			if (menuItem.getShortcut() != null)
				hasShortcuts = true;
		}
		
		if (hasIcons)
			add(AttributeAppender.append("class", "has-icons"));
		if (hasShortcuts)
			add(AttributeAppender.append("class", "has-shortcuts"));
		
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
					WebMarkupContainer link = menuItem.newLink("link");
					link.add(new SpriteImage("icon", menuItem.getIconHref()));
					link.add(new Label("label", menuItem.getLabel()));
					link.add(new Label("shortcut", menuItem.getShortcut()));
					fragment.add(link);
					item.add(fragment);
				} else {
					item.add(new Label("content", "<div></div>").setEscapeModelStrings(false));
					item.add(AttributeAppender.append("class", "dropdown-divider"));
				}
			}
			
		});
	}
	
	protected abstract List<MenuItem> getMenuItems();

}
