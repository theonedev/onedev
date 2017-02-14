package com.gitplex.commons.wicket.editable.polymorphic;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;

import com.gitplex.commons.wicket.editable.BeanContext;
import com.gitplex.commons.wicket.editable.EditableUtils;
import com.gitplex.commons.wicket.editable.PropertyDescriptor;
import com.gitplex.commons.wicket.editable.annotation.Horizontal;
import com.gitplex.commons.wicket.editable.annotation.Vertical;

@SuppressWarnings("serial")
public class PolymorphicPropertyViewer extends Panel {

	private final Serializable propertyValue;

	private final boolean vertical;
	
	public PolymorphicPropertyViewer(String id, PropertyDescriptor propertyDescriptor, Serializable propertyValue) {
		super(id);
		this.propertyValue = propertyValue;
		
		Method propertyGetter = propertyDescriptor.getPropertyGetter();
		if (propertyGetter.getAnnotation(Vertical.class) != null)
			vertical = true;
		else if (propertyGetter.getAnnotation(Horizontal.class) != null)
			vertical = false;
		else 
			vertical = true;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Fragment fragment;
		if (vertical)
			fragment = new Fragment("content", "verticalFrag", this);
		else
			fragment = new Fragment("content", "horizontalFrag", this);
		if (vertical)
			fragment.add(AttributeAppender.append("class", " vertical"));
		else
			fragment.add(AttributeAppender.append("class", " horizontal"));
		add(fragment);
		
		fragment.add(new Label("type", EditableUtils.getName(propertyValue.getClass())));
		fragment.add(BeanContext.viewBean("beanViewer", propertyValue));
	}

}
