package com.pmease.commons.wicket.editable.polymorphic;

import java.io.Serializable;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.wicket.editable.BeanContext;

@SuppressWarnings("serial")
public class PolymorphicPropertyViewer extends Panel {

	private final Serializable propertyValue;
	
	public PolymorphicPropertyViewer(String id, Serializable propertyValue) {
		super(id);
		this.propertyValue = propertyValue;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("type", EditableUtils.getName(propertyValue.getClass())));
		add(BeanContext.viewBean("beanViewer", propertyValue));
	}

}
