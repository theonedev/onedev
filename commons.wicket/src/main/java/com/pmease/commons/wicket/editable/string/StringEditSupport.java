package com.pmease.commons.wicket.editable.string;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.editable.PropertyDescriptorImpl;
import com.pmease.commons.editable.annotation.Markdown;
import com.pmease.commons.editable.annotation.Multiline;
import com.pmease.commons.editable.annotation.Password;
import com.pmease.commons.wicket.component.MultilineText;
import com.pmease.commons.wicket.component.markdown.MarkdownViewer;
import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.EditSupport;
import com.pmease.commons.wicket.editable.NotDefinedLabel;
import com.pmease.commons.wicket.editable.PropertyContext;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.commons.wicket.editable.PropertyViewer;

@SuppressWarnings("serial")
public class StringEditSupport implements EditSupport {

	@Override
	public BeanContext<?> getBeanEditContext(Class<?> beanClass) {
		return null;
	}

	@Override
	public PropertyContext<?> getPropertyEditContext(Class<?> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptorImpl(beanClass, propertyName);

		Class<?> propertyClass = propertyDescriptor.getPropertyGetter().getReturnType();
		if (propertyClass == String.class && propertyDescriptor.getPropertyGetter().getAnnotation(Password.class) == null) {
			return new PropertyContext<String>(propertyDescriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<String> model) {
					return new PropertyViewer(componentId, this) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
							if (model.getObject() != null) {
								if (propertyDescriptor.getPropertyGetter().getAnnotation(Multiline.class) != null)
									return new MultilineText(id, model.getObject());
								else if (propertyDescriptor.getPropertyGetter().getAnnotation(Markdown.class) != null)
									return new MarkdownViewer(id, model);
								else 
									return new Label(id, model.getObject());
							} else {
								return new NotDefinedLabel(id);
							}
						}
						
					};
				}

				@Override
				public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
					return new StringPropertyEditor(componentId, this, model);
				}
				
			};
		} else {
			return null;
		}
		
	}

}
