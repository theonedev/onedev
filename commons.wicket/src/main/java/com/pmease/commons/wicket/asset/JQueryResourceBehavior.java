package com.pmease.commons.wicket.asset;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;

@SuppressWarnings("serial")
public class JQueryResourceBehavior extends Behavior {

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		response.render(JQueryHeaderItem.get());
	}

}
