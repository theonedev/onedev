package com.pmease.gitop.web;

import java.util.Arrays;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.behavior.Behavior;
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

import com.pmease.commons.wicket.asset.CommonHeaderItem;
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
		
		/*
		 * Bind global resources here so that they can appear in page header before 
		 * any other resources. Simply rendering the resource in renderHead method of 
		 * base page will not work as renderHead method of container will be called 
		 * after contained components, and this will cause components with resources 
		 * using global resources not working properly.
		 *   
		 */
		add(new WebMarkupContainer("globalResourceBinder").add(new Behavior() {

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);

				response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(AssetLocator.class, "page.js") {

					@Override
					public Iterable<? extends HeaderItem> getDependencies() {
						return Arrays.asList(
								CommonHeaderItem.get(),
								CssHeaderItem.forReference(new CssResourceReference(AssetLocator.class, "page.css")));
					}
					
				}));
			}
			
		}));
	}
	
	protected abstract String getTitle();
	
	protected int getPageRefreshInterval() {
		return 0;
	}
}
