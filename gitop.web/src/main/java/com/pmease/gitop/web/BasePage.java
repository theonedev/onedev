package com.pmease.gitop.web;

import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.wicket.asset.CommonResourceReference;
import com.pmease.gitop.core.Gitop;

@SuppressWarnings("serial")
public abstract class BasePage extends WebPage {

	public BasePage() {
		if (!Gitop.getInstance().isReady() && getClass() != InitPage.class)
			throw new RestartResponseAtInterceptPageException(InitPage.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("title", getTitle()));
	}
	
	protected abstract String getTitle();
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new CommonResourceReference()));
	}
	
}
