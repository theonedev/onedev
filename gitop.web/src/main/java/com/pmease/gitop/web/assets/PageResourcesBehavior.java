package com.pmease.gitop.web.assets;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

public class PageResourcesBehavior extends Behavior {

	private static final long serialVersionUID = 1L;

	private static final PageResourcesBehavior instance = new PageResourcesBehavior();
	
	public static final PageResourcesBehavior get() {
		return instance;
	}
	
	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(JavaScriptHeaderItem.forReference(AssetLocator.PAGE_JS));
		response.render(CssHeaderItem.forReference(AssetLocator.PAGE_CSS));
	}
}
