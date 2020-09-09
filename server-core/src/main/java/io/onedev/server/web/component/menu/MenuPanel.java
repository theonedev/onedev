package io.onedev.server.web.component.menu;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
		AtomicBoolean hasIcons = new AtomicBoolean(false);
		AtomicBoolean hasShortcuts = new AtomicBoolean(false);
		AtomicBoolean hasSelections = new AtomicBoolean(false);
		for (MenuItem menuItem: menuItems) {
			if (menuItem != null) {
				if (menuItem.getIconHref() != null)
					hasIcons.set(true);
				if (menuItem.getShortcut() != null)
					hasShortcuts.set(true);
				if (menuItem.isSelected())
					hasSelections.set(true);
			}
		}
		
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
					if (menuItem.isSelected())
						link.add(new SpriteImage("tick", "tick"));
					else if (hasSelections.get())
						link.add(new SpriteImage("tick"));
					else
						link.add(new WebMarkupContainer("tick").setVisible(false));
					
					if (menuItem.getIconHref() != null) 
						link.add(new SpriteImage("icon", menuItem.getIconHref()));
					else if (hasIcons.get()) 
						link.add(new SpriteImage("icon"));
					else 
						link.add(new WebMarkupContainer("icon").setVisible(false));
					
					link.add(new Label("label", menuItem.getLabel()));
					
					if (menuItem.getShortcut() != null)
						link.add(new Label("shortcut", menuItem.getShortcut()));
					else if (hasShortcuts.get())
						link.add(new Label("shortcut", " "));
					else
						link.add(new WebMarkupContainer("shortcut").setVisible(false));
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
