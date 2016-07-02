package com.pmease.commons.wicket.component.loadingindicator;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.resource.CssResourceReference;

@SuppressWarnings("serial")
public class LoadingIndicator extends Panel {

	public LoadingIndicator(String id) {
		super(id);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(
				LoadingIndicator.class, "loading-indicator.css")));
	}

}
