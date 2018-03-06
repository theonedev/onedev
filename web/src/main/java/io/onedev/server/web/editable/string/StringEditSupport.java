package io.onedev.server.web.editable.string;

import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.util.editable.annotation.BranchPattern;
import io.onedev.server.util.editable.annotation.GroupChoice;
import io.onedev.server.util.editable.annotation.Markdown;
import io.onedev.server.util.editable.annotation.Multiline;
import io.onedev.server.util.editable.annotation.Password;
import io.onedev.server.util.editable.annotation.PathPattern;
import io.onedev.server.util.editable.annotation.ReviewRequirementSpec;
import io.onedev.server.util.editable.annotation.TagPattern;
import io.onedev.server.util.editable.annotation.UserChoice;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.NotDefinedLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;

@SuppressWarnings("serial")
public class StringEditSupport implements EditSupport {

	@Override
	public BeanContext<?> getBeanEditContext(Class<?> beanClass, Set<String> excludeProperties) {
		return null;
	}

	@Override
	public PropertyContext<?> getPropertyEditContext(Class<?> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptor(beanClass, propertyName);

		Class<?> propertyClass = propertyDescriptor.getPropertyGetter().getReturnType();
		if (propertyClass == String.class 
				&& propertyDescriptor.getPropertyGetter().getAnnotation(Password.class) == null
				&& propertyDescriptor.getPropertyGetter().getAnnotation(BranchPattern.class) == null
				&& propertyDescriptor.getPropertyGetter().getAnnotation(TagPattern.class) == null
				&& propertyDescriptor.getPropertyGetter().getAnnotation(GroupChoice.class) == null
				&& propertyDescriptor.getPropertyGetter().getAnnotation(UserChoice.class) == null
				&& propertyDescriptor.getPropertyGetter().getAnnotation(PathPattern.class) == null
				&& propertyDescriptor.getPropertyGetter().getAnnotation(ReviewRequirementSpec.class) == null) {
			return new PropertyContext<String>(propertyDescriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<String> model) {
					return new PropertyViewer(componentId, this) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
							if (model.getObject() != null) {
								if (propertyDescriptor.getPropertyGetter().getAnnotation(Multiline.class) != null) {
									return new MultilineLabel(id, model.getObject());
								} else if (propertyDescriptor.getPropertyGetter().getAnnotation(Markdown.class) != null) {
									return new MarkdownViewer(id, Model.of(model.getObject()), null);
								} else { 
									return new Label(id, model.getObject());
								}
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
