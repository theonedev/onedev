package io.onedev.server.web.editable;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

import io.onedev.server.web.util.AjaxPayload;

public class BeanUpdating extends AjaxPayload {
	
	private final PropertyUpdating source;
	
	public BeanUpdating(IPartialPageRequestHandler target, PropertyUpdating source) {
		super(target);
		this.source = source;
	}

	public PropertyUpdating getSource() {
		return source;
	}

}