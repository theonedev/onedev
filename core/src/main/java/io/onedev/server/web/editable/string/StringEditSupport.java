package io.onedev.server.web.editable.string;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;
import io.onedev.server.web.editable.annotation.BranchPattern;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Color;
import io.onedev.server.web.editable.annotation.TeamChoice;
import io.onedev.server.web.editable.annotation.IssueQuery;
import io.onedev.server.web.editable.annotation.Markdown;
import io.onedev.server.web.editable.annotation.Multiline;
import io.onedev.server.web.editable.annotation.Password;
import io.onedev.server.web.editable.annotation.PathPattern;
import io.onedev.server.web.editable.annotation.ReviewRequirement;
import io.onedev.server.web.editable.annotation.Script;
import io.onedev.server.web.editable.annotation.TagPattern;
import io.onedev.server.web.editable.annotation.UserChoice;

@SuppressWarnings("serial")
public class StringEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Class<?> propertyClass = descriptor.getPropertyGetter().getReturnType();
		if (propertyClass == String.class 
				&& descriptor.getPropertyGetter().getAnnotation(ChoiceProvider.class) == null
				&& descriptor.getPropertyGetter().getAnnotation(Markdown.class) == null
				&& descriptor.getPropertyGetter().getAnnotation(Script.class) == null
				&& descriptor.getPropertyGetter().getAnnotation(Color.class) == null
				&& descriptor.getPropertyGetter().getAnnotation(Password.class) == null
				&& descriptor.getPropertyGetter().getAnnotation(BranchPattern.class) == null
				&& descriptor.getPropertyGetter().getAnnotation(TagPattern.class) == null
				&& descriptor.getPropertyGetter().getAnnotation(IssueQuery.class) == null
				&& descriptor.getPropertyGetter().getAnnotation(TeamChoice.class) == null
				&& descriptor.getPropertyGetter().getAnnotation(UserChoice.class) == null
				&& descriptor.getPropertyGetter().getAnnotation(PathPattern.class) == null
				&& descriptor.getPropertyGetter().getAnnotation(ReviewRequirement.class) == null) {
			return new PropertyContext<String>(descriptor) {

				@Override
				public PropertyViewer renderForView(String componentId, final IModel<String> model) {
					return new PropertyViewer(componentId, descriptor) {

						@Override
						protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
							if (model.getObject() != null) {
								if (propertyDescriptor.getPropertyGetter().getAnnotation(Multiline.class) != null) {
									return new MultilineLabel(id, model.getObject());
								} else { 
									return new Label(id, model.getObject());
								}
							} else {
								return new EmptyValueLabel(id, propertyDescriptor.getPropertyGetter());
							}
						}
						
					};
				}

				@Override
				public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
					return new StringPropertyEditor(componentId, descriptor, model);
				}
				
			};
		} else {
			return null;
		}
		
	}

}
