package io.onedev.server.web.editable.choice;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.onedev.server.util.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;

@SuppressWarnings("serial")
public class ChoiceEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(Class<?> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptor(beanClass, propertyName);

		Class<?> propertyClass = propertyDescriptor.getPropertyGetter().getReturnType();
		if (propertyDescriptor.getPropertyGetter().getAnnotation(ChoiceProvider.class) != null) {
			if (propertyClass == String.class) {
				return new PropertyContext<String>(propertyDescriptor) {
	
					@Override
					public PropertyViewer renderForView(String componentId, final IModel<String> model) {
						return new PropertyViewer(componentId, this) {
	
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
						return new SingleChoiceEditor(componentId, this, model);
					}
					
				};
			} else if (List.class.isAssignableFrom(propertyDescriptor.getPropertyClass())) {
	            return new PropertyContext<List<String>>(propertyDescriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<List<String>> model) {

						return new PropertyViewer(componentId, this) {

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
						return new MultiChoiceEditor(componentId, this, model);
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

}
