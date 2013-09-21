package com.pmease.gitop.web;

import java.util.Arrays;

import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.wicket.asset.CommonResourceReference;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.web.asset.AssetLocator;

@SuppressWarnings("serial")
public abstract class BasePage extends WebPage {

	public BasePage() {
		if (!Gitop.getInstance().isReady() && getClass() != ServerInitPage.class)
			throw new RestartResponseAtInterceptPageException(ServerInitPage.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("title", getTitle()));

		add(new WebMarkupContainer("refresh") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.put("content", getPageRefreshInterval());
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPageRefreshInterval() != 0);
			}

		});
	}
	
	protected abstract String getTitle();
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(AssetLocator.class, "page.js") {

			@SuppressWarnings("unchecked")
			@Override
			public Iterable<? extends HeaderItem> getDependencies() {
				return Arrays.asList(
						JavaScriptHeaderItem.forReference(new CommonResourceReference()),
						CssHeaderItem.forReference(new CssResourceReference(AssetLocator.class, "page.css")));
			}
			
		}));
	}
	
	protected int getPageRefreshInterval() {
		return 0;
	}
}
