package com.pmease.commons.wicket;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;

@SuppressWarnings("serial")
class CommonResourcesBehavior extends Behavior {

	public static CommonResourcesBehavior get() {
		return INSTANCE;
	}
	
	private static CommonResourcesBehavior INSTANCE = new CommonResourcesBehavior();

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forReference(CommonResourcesReference.get())));
	}

}
