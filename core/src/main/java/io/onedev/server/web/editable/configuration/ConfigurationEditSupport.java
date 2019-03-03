package io.onedev.server.web.editable.configuration;

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
import io.onedev.server.web.editable.annotation.ConfigurationChoice;

@SuppressWarnings("serial")
public class ConfigurationEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
        Method propertyGetter = descriptor.getPropertyGetter();
        ConfigurationChoice configurationChoice = propertyGetter.getAnnotation(ConfigurationChoice.class);
        if (configurationChoice != null) {
        	if (List.class.isAssignableFrom(propertyGetter.getReturnType()) 
        			&& EditableUtils.getElementClass(propertyGetter.getGenericReturnType()) == String.class) {
        		return new PropertyContext<List<String>>(descriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<List<String>> model) {
						return new PropertyViewer(componentId, descriptor) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
						        List<String> configurations = model.getObject();
						        if (configurations != null && !configurations.isEmpty()) {
						            return new Label(id, StringUtils.join(configurations, ", " ));
						        } else {
									return new EmptyValueLabel(id, propertyDescriptor.getPropertyGetter());
						        }
							}
							
						};
					}

					@Override
					public PropertyEditor<List<String>> renderForEdit(String componentId, IModel<List<String>> model) {
						return new ConfigurationMultiChoiceEditor(componentId, descriptor, model);
					}
        			
        		};
        	} else if (propertyGetter.getReturnType() == String.class) {
        		return new PropertyContext<String>(descriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<String> model) {
						return new PropertyViewer(componentId, descriptor) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
						        String configurationName = model.getObject();
						        if (configurationName != null) {
						            return new Label(id, configurationName);
						        } else {
									return new EmptyValueLabel(id, propertyDescriptor.getPropertyGetter());
						        }
							}
							
						};
					}

					@Override
					public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
						return new ConfigurationSingleChoiceEditor(componentId, descriptor, model);
					}
        			
        		};
        	} else {
        		throw new RuntimeException("Annotation 'ConfigurationChoice' should be applied to property with type "
        				+ "'List<String> or String'");
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
