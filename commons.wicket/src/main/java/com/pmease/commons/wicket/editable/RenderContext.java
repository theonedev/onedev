package com.pmease.commons.wicket.editable;

import org.apache.wicket.MarkupContainer;

public class RenderContext {
	
	private final MarkupContainer container;
	
	private final String componentId;
	
	public RenderContext(MarkupContainer container, String componentId) {
		this.container = container;
		this.componentId = componentId;
	}

	public MarkupContainer getContainer() {
		return container;
	}

	public String getComponentId() {
		return componentId;
	}
	
}
