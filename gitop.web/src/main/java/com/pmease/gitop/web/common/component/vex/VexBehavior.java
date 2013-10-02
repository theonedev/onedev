package com.pmease.gitop.web.common.component.vex;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

@SuppressWarnings("serial")
public class VexBehavior extends Behavior {

	static final ResourceReference VEX_JS = new JavaScriptResourceReference(VexBehavior.class, "js/vex.combined.js");
	static final ResourceReference VEX_CSS = new CssResourceReference(VexBehavior.class, "css/vex.css");
	static final ResourceReference VEX_THEME_DEFAULT_CSS = new CssResourceReference(VexBehavior.class, "css/vex-theme-default.css");
	
	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(JavaScriptHeaderItem.forReference(VEX_JS));
		response.render(CssHeaderItem.forReference(VEX_CSS));
		response.render(CssHeaderItem.forReference(VEX_THEME_DEFAULT_CSS));
	}
}
