package io.onedev.server.web.editable.groupchoice;

import java.lang.reflect.Method;
import java.util.Collection;

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
import io.onedev.server.web.editable.annotation.GroupChoice;

@SuppressWarnings("serial")
public class GroupEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Method propertyGetter = descriptor.getPropertyGetter();
        if (propertyGetter.getAnnotation(GroupChoice.class) != null) {
        	if (Collection.class.isAssignableFrom(propertyGetter.getReturnType()) 
        			&& EditableUtils.getElementClass(propertyGetter.getGenericReturnType()) == String.class) {
        		return new PropertyContext<Collection<String>>(descriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<Collection<String>> model) {
						return new PropertyViewer(componentId, descriptor) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
								Collection<String> groupNames = model.getObject();
						        if (groupNames != null && !groupNames.isEmpty()) {
						            return new Label(id, StringUtils.join(groupNames, ", " ));
						        } else {
									return new EmptyValueLabel(id, propertyDescriptor.getPropertyGetter());
						        }
							}
							
						};
					}

					@Override
					public PropertyEditor<Collection<String>> renderForEdit(String componentId, IModel<Collection<String>> model) {
						return new GroupMultiChoiceEditor(componentId, descriptor, model);
					}
        			
        		};
        	} else if (propertyGetter.getReturnType() == String.class) {
        		return new PropertyContext<String>(descriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<String> model) {
						return new PropertyViewer(componentId, descriptor) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
						        String groupName = model.getObject();
						        if (groupName != null) {
						            return new Label(id, groupName);
						        } else {
									return new EmptyValueLabel(id, propertyDescriptor.getPropertyGetter());
						        }
							}
							
						};
					}

					@Override
					public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
						return new GroupSingleChoiceEditor(componentId, descriptor, model);
					}
        			
        		};
        	} else {
        		throw new RuntimeException("Annotation 'GroupChoice' should be applied to property with type String or Collection<String>.");
        	}
        } else {
            return null;
        }
	}

}
