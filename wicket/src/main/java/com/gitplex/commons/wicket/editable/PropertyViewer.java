package com.gitplex.commons.wicket.editable;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public abstract class PropertyViewer extends Panel {

	private final PropertyDescriptor propertyDescriptor;
	
	public PropertyViewer(String id, PropertyDescriptor propertyDescriptor) {
		super(id);
	
		this.propertyDescriptor = propertyDescriptor;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(newContent("content", propertyDescriptor));
		add(AttributeAppender.append("class", "property viewer editable"));
	}
	
	protected abstract Component newContent(String id, PropertyDescriptor propertyDescriptor);
}
