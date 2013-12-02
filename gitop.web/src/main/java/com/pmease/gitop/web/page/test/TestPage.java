package com.pmease.gitop.web.page.test;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;

import com.pmease.commons.wicket.behavior.dropdown.DropdownAlignment;
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
				Fragment frag = new Fragment(id, "test", TestPage.this);
				frag.add(new Label("label", "Hello, I'm in a lazy load panel"));
				return frag;
			}
			
		};
		add(dropdownPanel);
		DropdownBehavior dropdownBehavior = new DropdownBehavior(dropdownPanel);
		dropdownBehavior.alignment().indicatorMode(DropdownAlignment.IndicatorMode.SHOW);
		add(new WebMarkupContainer("dropdownTrigger").add(dropdownBehavior));
	}
	
	@Override
	protected String getPageTitle() {
		return "Test Page";
	}

}
