package io.onedev.server.web.editable.polymorphic;

import io.onedev.server.annotation.ExcludedProperties;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.PropertyDescriptor;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class PolymorphicPropertyViewer extends Panel {

	private final Serializable propertyValue;
	
	private final Set<String> excludedProperties = new HashSet<>();

	public PolymorphicPropertyViewer(String id, PropertyDescriptor descriptor, Serializable propertyValue) {
		super(id);
		this.propertyValue = propertyValue;

		ExcludedProperties excludedPropertiesAnnotation = 
				descriptor.getPropertyGetter().getAnnotation(ExcludedProperties.class);
		if (excludedPropertiesAnnotation != null) {
			for (String each: excludedPropertiesAnnotation.value())
				excludedProperties.add(each);
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("type", _T(EditableUtils.getDisplayName(propertyValue.getClass()))));
		add(new Label("typeDescription", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return EditableUtils.getDescription(propertyValue.getClass());
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(EditableUtils.getDescription(propertyValue.getClass()) != null);
			}
			
		});
		add(BeanContext.view("beanViewer", propertyValue, excludedProperties, true));
	}

}
