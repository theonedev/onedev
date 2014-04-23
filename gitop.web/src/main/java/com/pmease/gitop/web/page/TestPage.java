package com.pmease.gitop.web.page;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;

import com.pmease.commons.wicket.behavior.TooltipBehavior;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;

@SuppressWarnings("serial")
public class TestPage extends WebPage {

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new NotificationPanel("feedback"));
		
		add(new Link<Void>("link") {

			@Override
			public void onClick() {
				success("Setting is updated.");
			}
			
		}.add(new TooltipBehavior(new TooltipConfig().withSelector("span"))));
	}
	
}
