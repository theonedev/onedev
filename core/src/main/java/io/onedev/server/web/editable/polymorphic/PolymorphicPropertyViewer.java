package io.onedev.server.web.editable.polymorphic;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.apache.wicket.Application;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.annotation.Horizontal;
import io.onedev.server.web.editable.annotation.Vertical;

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
		
		String displayName = EditableUtils.getDisplayName(propertyValue.getClass());
		displayName = Application.get().getResourceSettings().getLocalizer().getString(displayName, this, displayName);
		
		fragment.add(new Label("type", displayName));
		fragment.add(BeanContext.viewBean("beanViewer", propertyValue));
	}

}
