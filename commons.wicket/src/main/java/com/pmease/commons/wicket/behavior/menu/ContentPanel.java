package com.pmease.commons.wicket.behavior.menu;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.wicket.behavior.dropdown.AlignmentTarget;
import com.pmease.commons.wicket.behavior.dropdown.DropdownAlignment;
import com.pmease.commons.wicket.behavior.dropdown.DropdownMode;

@SuppressWarnings("serial")
abstract class ContentPanel extends Panel {
	
	public ContentPanel(String id) {
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
				final MenuItem menuItem  = item.getModelObject();
				Component menuItemComponent = menuItem.newContent("item");
				if (menuItemComponent != null) {
					item.add(menuItemComponent.setOutputMarkupId(true));
					if (menuItem instanceof Menu) {
						final Menu menu = (Menu) menuItem;
						item.add(new AttributeModifier("class", "submenu"));
						MenuPanel menuPanel = new MenuPanel("itemMenu") {

							@Override
							protected List<MenuItem> getMenuItems() {
								return menu.getItems();
							}
							
						};
						menuPanel.add(AttributeModifier.append("class", "submenu"));
						item.add(menuPanel);
						AlignmentTarget target = new AlignmentTarget(menuItemComponent, 100, 0);
						DropdownAlignment alignment = new DropdownAlignment(target, 0, 0, -1, false);
						item.add(new MenuBehavior(menuPanel).mode(new DropdownMode.Hover(0)).alignment(alignment));
					} else {
						item.add(new WebMarkupContainer("itemMenu").setVisible(false));
					}
				} else {
					item.add(new AttributeModifier("class", "divider"));
					item.add(new WebMarkupContainer("item").setRenderBodyOnly(true));
					item.add(new WebMarkupContainer("itemMenu").setRenderBodyOnly(true));
				}
			}
			
		});
	}
	
	protected abstract List<MenuItem> getMenuItems();

}
