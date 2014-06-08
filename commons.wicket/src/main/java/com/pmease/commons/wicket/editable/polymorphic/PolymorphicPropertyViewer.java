package com.pmease.commons.wicket.editable.polymorphic;

import java.io.Serializable;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.BeanContext;

@SuppressWarnings("serial")
public class PolymorphicPropertyViewer extends Panel {

	private final PropertyDescriptor propertyDescriptor;
	
	private final Serializable propertyValue;
	
	public PolymorphicPropertyViewer(String id, PropertyDescriptor propertyDescriptor, Serializable propertyValue) {
		super(id);
		this.propertyDescriptor = propertyDescriptor;
		this.propertyValue = propertyValue;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("type", EditableUtils.getName(propertyDescriptor.getPropertyClass())));
		add(BeanContext.view("beanViewer", propertyValue));
	}

}
