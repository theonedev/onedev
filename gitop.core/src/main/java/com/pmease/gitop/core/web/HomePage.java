package com.pmease.gitop.core.web;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;

import com.pmease.commons.web.asset.bootstrap.BootstrapResourceReference;

@SuppressWarnings("serial")
public class HomePage extends WebPage {
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new BootstrapResourceReference()));
	}
	
}