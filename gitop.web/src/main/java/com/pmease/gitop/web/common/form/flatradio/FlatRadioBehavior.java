package com.pmease.gitop.web.common.form.flatradio;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

@SuppressWarnings("serial")
public class FlatRadioBehavior extends Behavior {

	private static final ResourceReference FLATUI_RADIO_JS =
			new JavaScriptResourceReference(FlatRadioBehavior.class, "flatui-radio.js");
	
	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(JavaScriptHeaderItem.forReference(FLATUI_RADIO_JS));
	}
}
