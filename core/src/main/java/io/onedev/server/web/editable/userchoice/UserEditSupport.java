package io.onedev.server.web.editable.userchoice;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.util.StringUtils;

import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;
import io.onedev.server.web.editable.annotation.UserChoice;

@SuppressWarnings("serial")
public class UserEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(Class<?> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptor(beanClass, propertyName);
        Method propertyGetter = propertyDescriptor.getPropertyGetter();
        UserChoice userChoice = propertyGetter.getAnnotation(UserChoice.class);
        if (userChoice != null) {
        	if (List.class.isAssignableFrom(propertyGetter.getReturnType()) 
        			&& EditableUtils.getElementClass(propertyGetter.getGenericReturnType()) == String.class) {
        		return new PropertyContext<List<String>>(propertyDescriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<List<String>> model) {
						return new PropertyViewer(componentId, this) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
						        List<String> userNames = model.getObject();
						        if (userNames != null && !userNames.isEmpty()) {
						            return new Label(id, StringUtils.join(userNames, ", " ));
						        } else {
									return new EmptyValueLabel(id, propertyDescriptor.getPropertyGetter());
						        }
							}
							
						};
					}

					@Override
					public PropertyEditor<List<String>> renderForEdit(String componentId, IModel<List<String>> model) {
						return new UserMultiChoiceEditor(componentId, this, model);
					}
        			
        		};
        	} else if (propertyGetter.getReturnType() == String.class) {
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
						return new UserSingleChoiceEditor(componentId, this, model);
					}
        			
        		};
        	} else {
        		throw new RuntimeException("Annotation 'UserChoice' should be applied to property with type 'String' or 'List<String>'.");
        	}
        } else {
            return null;
        }
	}

}
