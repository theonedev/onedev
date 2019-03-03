package io.onedev.server.web.editable.choice;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;
import io.onedev.server.web.editable.annotation.ChoiceProvider;

@SuppressWarnings("serial")
public class ChoiceEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Class<?> propertyClass = descriptor.getPropertyGetter().getReturnType();
		if (descriptor.getPropertyGetter().getAnnotation(ChoiceProvider.class) != null) {
			if (propertyClass == String.class) {
				return new PropertyContext<String>(descriptor) {
	
					@Override
					public PropertyViewer renderForView(String componentId, final IModel<String> model) {
						return new PropertyViewer(componentId, descriptor) {
	
							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
								if (model.getObject() != null) {
									return new Label(id, model.getObject());
								} else {
									return new EmptyValueLabel(id, propertyDescriptor.getPropertyGetter());
								}
							}
							
						};
					}
	
					@Override
					public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
						return new SingleChoiceEditor(componentId, descriptor, model);
					}
					
				};
			} else if (List.class.isAssignableFrom(descriptor.getPropertyClass())) {
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
									return new EmptyValueLabel(id, propertyDescriptor.getPropertyGetter());
						        }
							}
							
						};
					}

					@Override
					public PropertyEditor<List<String>> renderForEdit(String componentId, IModel<List<String>> model) {
						return new MultiChoiceEditor(componentId, descriptor, model);
					}
	            	
	            };
			} else {
				throw new IllegalStateException("@ChoiceProvider annotation should only be "
						+ "applied to a String or List<String> property");
			}
		} else {
			return null;
		}
		
	}

	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY;
	}
	
}
