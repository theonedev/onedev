package io.onedev.server.web.editable;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

import io.onedev.server.web.util.AjaxPayload;

public class PropertyUpdating extends AjaxPayload {

	private final String propertyName;
	
	public PropertyUpdating(IPartialPageRequestHandler target, String propertyName) {
		super(target);
		this.propertyName = propertyName;
	}

	public String getPropertyName() {
		return propertyName;
	}

}