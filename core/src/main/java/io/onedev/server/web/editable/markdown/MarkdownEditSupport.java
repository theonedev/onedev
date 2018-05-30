package io.onedev.server.web.editable.markdown;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;
import io.onedev.server.web.editable.annotation.Markdown;

@SuppressWarnings("serial")
public class MarkdownEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(Class<?> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptor(beanClass, propertyName);

		Class<?> propertyClass = propertyDescriptor.getPropertyGetter().getReturnType();
		if (propertyClass == String.class 
				&& propertyDescriptor.getPropertyGetter().getAnnotation(Markdown.class) != null) {
			return new PropertyContext<String>(propertyDescriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<String> model) {
					return new PropertyViewer(componentId, this) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
							return new MarkdownViewer(id, Model.of(model.getObject()), null);
						}
						
					};
				}

				@Override
				public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
					return new MarkdownPropertyEditor(componentId, this, model);
				}
				
			};
		} else {
			return null;
		}
		
	}

}
