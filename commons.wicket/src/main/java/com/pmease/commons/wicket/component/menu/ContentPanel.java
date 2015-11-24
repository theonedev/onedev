package com.pmease.commons.wicket.component.menu;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.wicket.behavior.dropdown.DropdownHover;
import com.pmease.commons.wicket.component.floating.AlignPlacement;
import com.pmease.commons.wicket.component.floating.FloatingPanel;

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
						item.add(new DropdownHover(menuItemComponent, new AlignPlacement(100, 0, 0, 0)) {

							@Override
							protected void onInitialize(FloatingPanel dropdown) {
								super.onInitialize(dropdown);
								dropdown.add(AttributeAppender.append("class", " submenu"));
							}

							@Override
							protected Component newContent(String id) {
								return new ContentPanel(id) {

									@Override
									protected List<MenuItem> getMenuItems() {
										return menu.getItems();
									}
									
								};
							}
							
						});
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
