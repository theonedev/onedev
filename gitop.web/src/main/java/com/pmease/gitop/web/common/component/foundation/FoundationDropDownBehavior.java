package com.pmease.gitop.web.common.component.foundation;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

public class FoundationDropDownBehavior extends Behavior {

	private static final long serialVersionUID = 1L;

	@Override
	public void renderHead(final Component component, final IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(JavaScriptHeaderItem.forReference(FoundationDropDownResourceReference.get()));
	}
}
