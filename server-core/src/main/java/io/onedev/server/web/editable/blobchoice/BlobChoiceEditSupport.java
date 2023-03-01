package io.onedev.server.web.editable.blobchoice;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;
import io.onedev.server.annotation.BlobChoice;

@SuppressWarnings("serial")
public class BlobChoiceEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
        Method propertyGetter = descriptor.getPropertyGetter();
        if (propertyGetter.getAnnotation(BlobChoice.class) != null) {
        	if (propertyGetter.getReturnType() == String.class) {
        		return new PropertyContext<String>(descriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<String> model) {
						return new PropertyViewer(componentId, descriptor) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
								if (model.getObject() != null) {
						            return new Label(id, model.getObject());
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
					public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
						return new BlobChoiceEditor(componentId, descriptor, model);
					}
        			
        		};
        	} else {
        		throw new RuntimeException("Annotation 'BlobChoice' should be applied to property with type 'String'");
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
