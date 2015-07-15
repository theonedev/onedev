package com.pmease.gitplex.web.page.test;

import com.pmease.gitplex.web.page.base.BasePage;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.DateTextField;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new DateTextField("date"));
	}
	
}
