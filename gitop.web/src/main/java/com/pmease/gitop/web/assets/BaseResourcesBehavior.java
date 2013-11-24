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

//	static final ResourceReference MODERNIZR_JS = new JavaScriptResourceReference(BaseResourceBehavior.class, "js/vendor/modernizr-2.6.2.js");
//	static final ResourceReference PAGE_JS = new JavaScriptResourceReference(BaseResourceBehavior.class, "js/page.js");
//	
//	static final ResourceReference FONT_AWESOME_CSS = new CssResourceReference(BaseResourceBehavior.class, "css/font-awesome.css");
//	static final ResourceReference FONT_AWESOME_MIN_CSS = new CssResourceReference(BaseResourceBehavior.class, "css/font-awesome.min.css");
//	static final ResourceReference BASE_CSS = new CssResourceReference(BaseResourceBehavior.class, "css/base.css");
//	static final ResourceReference PAGE_CSS = new CssResourceReference(BaseResourceBehavior.class, "css/page.css");
	
	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(new PriorityHeaderItem(JavaScriptReferenceHeaderItem.forReference(AssetLocator.MODERNIZR_JS)));
		
		// render jquery
		response.render(new PriorityHeaderItem(JQueryHeaderItem.get()));

		// render bootstrap
		response.render(new PriorityHeaderItem(BootstrapHeaderItem.get()));
		
		// render font-awesome
		response.render(new PriorityHeaderItem(CssReferenceHeaderItem.forReference(AssetLocator.ICONS_CSS)));
		
		response.render(new PriorityHeaderItem(CssReferenceHeaderItem.forReference(AssetLocator.BASE_CSS)));
	}
}
