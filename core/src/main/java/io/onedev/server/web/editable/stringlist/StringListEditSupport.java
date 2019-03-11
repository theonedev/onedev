package io.onedev.server.web.editable.stringlist;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.onedev.commons.utils.ReflectionUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@SuppressWarnings("serial")
public class StringListEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Method propertyGetter = descriptor.getPropertyGetter();
    	Class<?> returnType = propertyGetter.getReturnType();
    	Type genericReturnType = propertyGetter.getGenericReturnType();
    	if (returnType == List.class && ReflectionUtils.getCollectionElementType(genericReturnType) == String.class) {
    		return new PropertyContext<List<String>>(descriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<List<String>> model) {
					return new PropertyViewer(componentId, descriptor) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
					        List<String> tags = model.getObject();
					        if (tags != null && !tags.isEmpty()) {
					        	return new MultilineLabel(id, StringUtils.join(tags, "\n"));
					        } else {
								NameOfEmptyValue nameOfEmptyValue = propertyDescriptor.getPropertyGetter().getAnnotation(NameOfEmptyValue.class);
								if (nameOfEmptyValue != null)
									return new Label(id, nameOfEmptyValue.value());
								else 
									return new EmptyValueLabel(id, propertyDescriptor.getPropertyGetter());
					        }
						}
						
					};
				}

				@Override
				public PropertyEditor<List<String>> renderForEdit(String componentId, IModel<List<String>> model) {
		        	return new StringListPropertyEditor(componentId, descriptor, model);
				}
    			
    		};
        } else {
            return null;
        }
	}

	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY * 2;
	}
	
}
