package io.onedev.server.web.editable.enumeration;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;

@SuppressWarnings("serial")
public class EnumEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
        if (Enum.class.isAssignableFrom(descriptor.getPropertyClass())) {
            return new PropertyContext<Enum<?>>(descriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<Enum<?>> model) {
					return new PropertyViewer(componentId, descriptor) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
					        if (model.getObject() != null)
					            return new Label(id, StringUtils.capitalize(model.getObject().name().replace('_', ' ').toLowerCase()));
					        else 
								return new EmptyValueLabel(id, propertyDescriptor.getPropertyGetter());
						}
						
					};
				}

				@Override
				public PropertyEditor<Enum<?>> renderForEdit(String componentId, IModel<Enum<?>> model) {
					return new EnumPropertyEditor(componentId, descriptor, model);
				}
            	
            };
        } else {
            return null;
        }
	}

	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY;
	}
	
}
