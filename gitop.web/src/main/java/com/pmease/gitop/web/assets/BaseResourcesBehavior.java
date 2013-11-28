package com.pmease.gitop.web.assets;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;

import com.pmease.commons.wicket.asset.JQueryHeaderItem;
import com.pmease.commons.wicket.asset.bootstrap.BootstrapHeaderItem;

public class BaseResourcesBehavior extends Behavior {
	private static final long serialVersionUID = 1L;

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(new PriorityHeaderItem(JavaScriptReferenceHeaderItem.forReference(AssetLocator.MODERNIZR_JS)));
		
		// render jquery
		response.render(new PriorityHeaderItem(JQueryHeaderItem.get()));

		// render bootstrap
		response.render(new PriorityHeaderItem(BootstrapHeaderItem.get()));
		
		// render icons
		response.render(new PriorityHeaderItem(CssReferenceHeaderItem.forReference(AssetLocator.ICONS_CSS)));
		
		response.render(new PriorityHeaderItem(CssReferenceHeaderItem.forReference(AssetLocator.BASE_CSS)));
	}
}
