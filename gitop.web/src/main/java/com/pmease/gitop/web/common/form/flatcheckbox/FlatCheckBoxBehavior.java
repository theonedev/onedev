package com.pmease.gitop.web.common.form.flatcheckbox;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

@SuppressWarnings("serial")
public class FlatCheckBoxBehavior extends Behavior {

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(FlatCheckBoxBehavior.class, "flatui-checkbox.js")));
	}
}
