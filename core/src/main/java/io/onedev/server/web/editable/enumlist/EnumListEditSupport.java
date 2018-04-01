package io.onedev.server.web.editable.enumlist;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.onedev.server.util.editable.EditableUtils;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;

@SuppressWarnings("serial")
public class EnumListEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(Class<?> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptor(beanClass, propertyName);
		
		if (List.class.isAssignableFrom(propertyDescriptor.getPropertyClass())) {
			final Class<?> elementClass = EditableUtils.getElementClass(propertyDescriptor.getPropertyGetter().getGenericReturnType());
			if (elementClass != null && Enum.class.isAssignableFrom(elementClass)) {
	            return new PropertyContext<List<Enum<?>>>(propertyDescriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<List<Enum<?>>> model) {

						return new PropertyViewer(componentId, this) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
						        if (model.getObject() != null && !model.getObject().isEmpty()) {
						            String content = "";
						            for (Enum<?> each: model.getObject()) {
						            	if (content.length() == 0)
						            		content += each.toString();
						            	else
						            		content += ", " + each.toString();
						            }
						            return new Label(id, content);
						        } else { 
									return new EmptyValueLabel(id, propertyDescriptor.getPropertyGetter());
						        }
							}
							
						};
					}

					@Override
					public PropertyEditor<List<Enum<?>>> renderForEdit(String componentId, IModel<List<Enum<?>>> model) {
						return new EnumListPropertyEditor(componentId, this, model);
					}
	            	
	            };
			}
		}
		return null;
	}

}
