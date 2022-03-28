package io.onedev.server.web.editable.emaillist;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.util.validation.annotation.EmailList;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;

@SuppressWarnings("serial")
public class EmailListEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Method propertyGetter = descriptor.getPropertyGetter();
		if (propertyGetter.getAnnotation(EmailList.class) != null) {
        	if (List.class.isAssignableFrom(propertyGetter.getReturnType()) 
        			&& ReflectionUtils.getCollectionElementClass(propertyGetter.getGenericReturnType()) == String.class) {
	            return new PropertyContext<List<String>>(descriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<List<String>> model) {

						return new PropertyViewer(componentId, descriptor) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
								if (model.getObject() != null && !model.getObject().isEmpty()) {
						            return new Label(id, StringUtils.join(model.getObject(), ", "));
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
						return new EmailListPropertyEditor(componentId, descriptor, model);
					}
					
	            }; 		
        	} else {
        		throw new RuntimeException("Annotation 'EmailList' should be applied to property with type 'List<String>'.");
        	}
		}
		return null;
	}

	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY;
	}
	
}
