package com.pmease.gitop.web.page.test;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.gitop.web.page.AbstractLayoutPage;

@SuppressWarnings("serial")
public class TestPage extends AbstractLayoutPage {

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();

		DropdownPanel dropdownPanel = new DropdownPanel("dropdownPanel", true) {

			@Override
			protected Component newContent(String id) {
				return new Label(id, "hello world");
			}
			
		};
		add(dropdownPanel);
		add(new WebMarkupContainer("dropdownTrigger").add(new DropdownBehavior(dropdownPanel)));
	}
	
	@Override
	protected String getPageTitle() {
		return "Test Page";
	}

}
