package com.pmease.commons.wicket.asset.bootstrap;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;

@SuppressWarnings("serial")
public class BootstrapResourceBehavior extends Behavior {

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		response.render(BootstrapHeaderItem.get());
	}

}
