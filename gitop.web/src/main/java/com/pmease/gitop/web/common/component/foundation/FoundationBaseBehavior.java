package com.pmease.gitop.web.common.component.foundation;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.google.common.base.Preconditions;

public class FoundationBaseBehavior extends Behavior {

	private static final long serialVersionUID = 1L;

	private static final FoundationBaseBehavior instance = new FoundationBaseBehavior();
	
	public static void removeFrom(final Component component) {
		Preconditions.checkNotNull(component, "component");
		component.remove(instance);
	}
	
	public static void addTo(final Component component) {
		Preconditions.checkNotNull(component, "component");
		component.add(instance);
	}
	
	private final static ResourceReference FOUNDATION_JS = new JavaScriptResourceReference(FoundationBaseBehavior.class, "res/js/foundation.js");
	
	@Override
	public void renderHead(final Component component, final IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(JavaScriptHeaderItem.forReference(FOUNDATION_JS));
	}
}
