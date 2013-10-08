package com.pmease.gitop.web.common.component.dropzone;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

@SuppressWarnings("serial")
public class DropZoneBehavior extends Behavior {

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		response.render(JavaScriptHeaderItem.forReference(DropZoneResourceReference.get()));
	}
}
