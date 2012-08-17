package com.pmease.commons.product.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.wicket.behavior.menu.Menu;
import com.pmease.commons.wicket.behavior.menu.MenuBehavior;
import com.pmease.commons.wicket.behavior.menu.MenuDivider;
import com.pmease.commons.wicket.behavior.menu.MenuItem;
import com.pmease.commons.wicket.behavior.menu.MenuPanel;
import com.pmease.commons.wicket.page.CommonPage;

@SuppressWarnings("serial")
public class HomePage extends CommonPage  {
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		MenuPanel menuPanel = new MenuPanel("menu") {

			@Override
			protected List<MenuItem> getMenuItems() {
				List<MenuItem> menuItems = new ArrayList<MenuItem>();
				menuItems.add(new MenuItem() {

					@Override
					public Component newContent(String componentId) {
						return new Label(componentId, "item");
					}
					
				});
				menuItems.add(new MenuItem() {

					@Override
					public Component newContent(String componentId) {
						return new Label(componentId, "item");
					}
					
				});
				menuItems.add(new MenuDivider());
				menuItems.add(new Menu() {

					@Override
					public Component newContent(String componentId) {
						return new Label(componentId, "item");
					}

					@Override
					public List<MenuItem> getItems() {
						List<MenuItem> menuItems = new ArrayList<MenuItem>();
						menuItems.add(new MenuItem() {

							@Override
							public Component newContent(String componentId) {
								return new Label(componentId, "item");
							}
							
						});
						menuItems.add(new MenuItem() {

							@Override
							public Component newContent(String componentId) {
								return new Label(componentId, "item");
							}
							
						});
						return menuItems;
					}
					
				});
				return menuItems;
			}
			
		};
		
		add(menuPanel);
		
		add(new WebMarkupContainer("menuTrigger").add(new MenuBehavior(menuPanel)));
	}	
}