package io.onedev.server.web.editable.color;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;
import io.onedev.server.web.editable.annotation.Color;

@SuppressWarnings("serial")
public class ColorEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		if (descriptor.getPropertyGetter().getAnnotation(Color.class) != null) {
			return new PropertyContext<String>(descriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<String> model) {
					return new PropertyViewer(componentId, descriptor) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
							if (model.getObject() != null) {
								String html = String.format("<div style='width: 25px; height: 25px; background: %s; border-radius: 4px;'></div>", model.getObject());
								return new Label(id, html).setEscapeModelStrings(false);
							} else {
								return new EmptyValueLabel(id, propertyDescriptor.getPropertyGetter());
							}
						}
						
					};
				}

				@Override
				public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
					return new ColorPropertyEditor(componentId, descriptor, model);
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
