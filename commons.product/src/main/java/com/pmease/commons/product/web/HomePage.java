package com.pmease.commons.product.web;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.commons.wicket.page.CommonPage;

@SuppressWarnings("serial")
public class HomePage extends CommonPage  {
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		DropdownPanel dropdown = new DropdownPanel("dropdown", false) {

			@Override
			protected Component newContent(String id) {
				return new Label(id, "Hello World");
			}
			
		};
		add(dropdown);
		add(new WebMarkupContainer("toggle").add(new DropdownBehavior(dropdown)));
	}	
	
}