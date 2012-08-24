package com.pmease.commons.product.web;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import com.pmease.commons.wicket.behavior.popover.PopoverBehavior;
import com.pmease.commons.wicket.behavior.popover.PopoverPanel;
import com.pmease.commons.wicket.page.CommonPage;

@SuppressWarnings("serial")
public class HomePage extends CommonPage  {
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		PopoverPanel dropdown = new PopoverPanel("dropdown", Model.of("Hello world")) {

			@Override
			protected Component newBody(String id) {
				return new Label(id, "hello world just do it");
			}

		};
		
		add(dropdown);
		
		add(new WebMarkupContainer("toggle1").add(new PopoverBehavior(dropdown)));
		add(new WebMarkupContainer("toggle2").add(new PopoverBehavior(dropdown)));
	}	
}