package io.onedev.server.web.editable.polymorphic;

import java.io.Serializable;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;

import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.PropertyDescriptor;

@SuppressWarnings("serial")
public class PolymorphicPropertyViewer extends Panel {

	private final Serializable propertyValue;

	public PolymorphicPropertyViewer(String id, PropertyDescriptor propertyDescriptor, Serializable propertyValue) {
		super(id);
		this.propertyValue = propertyValue;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		String displayName = EditableUtils.getDisplayName(propertyValue.getClass());
		displayName = Application.get().getResourceSettings().getLocalizer().getString(displayName, this, displayName);
		
		add(new Label("type", displayName));
		add(new Label("typeDescription", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return EditableUtils.getDescription(propertyValue.getClass());
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(propertyValue != null && EditableUtils.getDescription(propertyValue.getClass()) != null);
			}
			
		});
		add(BeanContext.viewBean("beanViewer", propertyValue));
	}

}
