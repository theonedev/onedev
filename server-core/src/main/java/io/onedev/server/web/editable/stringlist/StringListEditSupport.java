package io.onedev.server.web.editable.stringlist;

import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.editable.*;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import java.lang.reflect.AnnotatedElement;
import java.util.List;

@SuppressWarnings("serial")
public class StringListEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		if (List.class.isAssignableFrom(descriptor.getPropertyClass()) 
				&& ReflectionUtils.getCollectionElementClass(descriptor.getPropertyGetter().getGenericReturnType()) == String.class) {
						
			return new PropertyContext<List<String>>(descriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<List<String>> model) {

					return new PropertyViewer(componentId, descriptor) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
							if (model.getObject() != null && !model.getObject().isEmpty()) {
								String content = "";
								for (String each: model.getObject()) {
									if (content.length() == 0)
										content += each.toString();
									else
										content += ", " + each.toString();
								}
								return new Label(id, content);
							} else { 
								return new EmptyValueLabel(id) {

									@Override
									protected AnnotatedElement getElement() {
										return propertyDescriptor.getPropertyGetter();
									}
									
								};
							}
						}
						
					};
				}

				@Override
				public PropertyEditor<List<String>> renderForEdit(String componentId, IModel<List<String>> model) {
					return new StringListEditor(componentId, descriptor, model);
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
