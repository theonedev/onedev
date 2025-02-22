package io.onedev.server.web.editable.image;

import io.onedev.server.annotation.Image;
import io.onedev.server.web.component.imagedata.viewer.ImageViewer;
import io.onedev.server.web.editable.*;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import java.lang.reflect.AnnotatedElement;

public class ImageEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		var image = descriptor.getPropertyGetter().getAnnotation(Image.class);
		if (image != null) {
			return new PropertyContext<String>(descriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<String> model) {
					return new PropertyViewer(componentId, descriptor) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
							if (model.getObject() != null) {
								return new ImageViewer(id, model, image.width(), image.height(), 
										image.backgroundColor());
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
					return new ImagePropertyEditor(componentId, descriptor, model);
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
