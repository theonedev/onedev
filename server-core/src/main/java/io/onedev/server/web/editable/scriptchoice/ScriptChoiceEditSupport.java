package io.onedev.server.web.editable.scriptchoice;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;
import io.onedev.server.web.editable.annotation.ScriptChoice;

@SuppressWarnings("serial")
public class ScriptChoiceEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Class<?> propertyClass = descriptor.getPropertyGetter().getReturnType();
		if (descriptor.getPropertyGetter().getAnnotation(ScriptChoice.class) != null) {
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
						return new ScriptChoiceEditor(componentId, descriptor, model);
					}
					
				};
			} else {
				throw new IllegalStateException("@ScriptChoice annotation should only be "
						+ "applied to a String property");
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
