package io.onedev.server.web.editable.enumlist;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;

@SuppressWarnings("serial")
public class EnumListEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		if (List.class.isAssignableFrom(descriptor.getPropertyClass())) {
			final Class<?> elementClass = ReflectionUtils.getCollectionElementType(descriptor.getPropertyGetter().getGenericReturnType());
			if (elementClass != null && Enum.class.isAssignableFrom(elementClass)) {
	            return new PropertyContext<List<Enum<?>>>(descriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<List<Enum<?>>> model) {

						return new PropertyViewer(componentId, descriptor) {

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
						return new EnumListPropertyEditor(componentId, descriptor, model);
					}
	            	
	            };
			}
		}
		return null;
	}

	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY;
	}
	
}
