package io.onedev.server.web.editable.string;

import org.apache.wicket.model.IModel;

import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;

@SuppressWarnings("serial")
public class StringEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Class<?> propertyClass = descriptor.getPropertyGetter().getReturnType();
		if (propertyClass == String.class) {
			return new PropertyContext<String>(descriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<String> model) {
					return new StringPropertyViewer(componentId, descriptor, model.getObject());
				}

				@Override
				public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
					return new StringPropertyEditor(componentId, descriptor, model);
				}
				
			};
		} else {
			return null;
		}
		
	}

	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY*2;
	}
	
}
