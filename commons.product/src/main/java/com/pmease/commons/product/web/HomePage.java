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

		MenuPanel menu = new MenuPanel("menu") {

			@Override
			protected List<MenuItem> getMenuItems() {
				List<MenuItem> items = new ArrayList<MenuItem>();
				items.add(new MenuItem() {

					@Override
					public Component newContent(String componentId) {
						return new Label(componentId, "New Action 1");
					}
					
				});
				items.add(new MenuItem() {

					@Override
					public Component newContent(String componentId) {
						return new Label(componentId, "Some Else New Action 2");
					}
					
				});
				items.add(new MenuItem() {

					@Override
					public Component newContent(String componentId) {
						return new Label(componentId, "New Action 3");
					}
					
				});
				items.add(new MenuDivider());
				items.add(new Menu() {

					@Override
					public List<MenuItem> getItems() {
						List<MenuItem> items = new ArrayList<MenuItem>();
						items.add(new MenuItem() {

							@Override
							public Component newContent(String componentId) {
								return new Label(componentId, "New Action 1");
							}
							
						});
						items.add(new MenuItem() {

							@Override
							public Component newContent(String componentId) {
								return new Label(componentId, "New Action 2");
							}
							
						});
						items.add(new MenuItem() {

							@Override
							public Component newContent(String componentId) {
								return new Label(componentId, "New Action 3");
							}
							
						});
						return items;
					}

					@Override
					public Component newContent(String componentId) {
						return new Label(componentId, "Sub Menu 1");
					}
					
				});
				return items;
			}

		};
		
		add(menu);
		
		add(new WebMarkupContainer("toggle1").add(new MenuBehavior(menu)));
		add(new WebMarkupContainer("toggle2").add(new MenuBehavior(menu)));
	}	
}