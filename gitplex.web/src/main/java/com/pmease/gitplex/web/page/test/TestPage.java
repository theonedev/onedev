package com.pmease.gitplex.web.page.test;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;

import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();

		RepeatingView repeatingView = new RepeatingView("list");
		WebMarkupContainer child = new WebMarkupContainer(repeatingView.newChildId());
		child.add(new Label("name", "robin"));
		child.setOutputMarkupId(true);
		repeatingView.add(child);
		add(repeatingView);
	}
	
}
