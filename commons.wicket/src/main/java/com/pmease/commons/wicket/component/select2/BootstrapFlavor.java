package com.pmease.commons.wicket.component.select2;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.resource.CssResourceReference;

@SuppressWarnings("serial")
public class BootstrapFlavor extends Behavior {

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(BootstrapFlavor.class, "select2-bootstrap.css")));
	}

}
