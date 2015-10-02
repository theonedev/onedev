package com.pmease.commons.wicket.editable.bool;

import java.lang.reflect.Method;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.DefaultPropertyDescriptor;
import com.pmease.commons.wicket.editable.EditSupport;
import com.pmease.commons.wicket.editable.NotDefinedLabel;
import com.pmease.commons.wicket.editable.PropertyContext;
import com.pmease.commons.wicket.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.commons.wicket.editable.PropertyViewer;

@SuppressWarnings("serial")
public class BooleanEditSupport implements EditSupport {

	@Override
	public BeanContext<?> getBeanEditContext(Class<?> beanClass) {
		return null;
	}

	@Override
	public PropertyContext<?> getPropertyEditContext(Class<?> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new DefaultPropertyDescriptor(beanClass, propertyName);
		
		Method propertyGetter = propertyDescriptor.getPropertyGetter();
		
		Class<?> propertyClass = propertyGetter.getReturnType();
		if (propertyClass == boolean.class || propertyClass == Boolean.class) {
			return new PropertyContext<Boolean>(propertyDescriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<Boolean> model) {
					return new PropertyViewer(componentId, this) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
							if (model.getObject() != null) {
								return new Label(id, model.getObject().toString());
							} else {
								return new NotDefinedLabel(id);
							}
						}
						
					};
				}

				@Override
				public PropertyEditor<Boolean> renderForEdit(String componentId, IModel<Boolean> model) {
					if (isPropertyRequired())
						return new BooleanPropertyEditor(componentId, this, model);
					else
						return new NullableBooleanPropertyEditor(componentId, this, model);
				}
				
			};
		} else {
			return null;
		}
	}

}
