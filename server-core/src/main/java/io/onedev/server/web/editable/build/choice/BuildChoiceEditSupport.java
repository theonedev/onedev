package io.onedev.server.web.editable.build.choice;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;
import io.onedev.server.web.editable.annotation.BuildChoice;

@SuppressWarnings("serial")
public class BuildChoiceEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
        Method propertyGetter = descriptor.getPropertyGetter();
        BuildChoice buildChoice = propertyGetter.getAnnotation(BuildChoice.class);
        if (buildChoice != null) {
        	if (List.class.isAssignableFrom(propertyGetter.getReturnType()) 
        			&& ReflectionUtils.getCollectionElementType(propertyGetter.getGenericReturnType()) == Long.class) {
        		return new PropertyContext<List<Long>>(descriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<List<Long>> model) {
						return new PropertyViewer(componentId, descriptor) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
						        List<Long> buildIds = model.getObject();
						        if (buildIds != null && !buildIds.isEmpty()) {
						        	return new Label(id, buildIds.stream().map(it->"#"+it).collect(Collectors.joining(", ")));
						        } else {
									return new EmptyValueLabel(id, propertyDescriptor.getPropertyGetter());
						        }
							}
							
						};
					}

					@Override
					public PropertyEditor<List<Long>> renderForEdit(String componentId, IModel<List<Long>> model) {
						return new BuildMultiChoiceEditor(componentId, descriptor, model);
					}
        			
        		};
        	} else if (propertyGetter.getReturnType() == Long.class) {
        		return new PropertyContext<Long>(descriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<Long> model) {
						return new PropertyViewer(componentId, descriptor) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
								if (model.getObject() != null) 
						            return new Label(id, "#" + model.getObject());
						        else 
									return new EmptyValueLabel(id, propertyDescriptor.getPropertyGetter());
							}
							
						};
					}

					@Override
					public PropertyEditor<Long> renderForEdit(String componentId, IModel<Long> model) {
						return new BuildSingleChoiceEditor(componentId, descriptor, model);
					}
        			
        		};
        	} else {
        		throw new RuntimeException("Annotation 'BuildChoice' should be applied to property with type 'Long' or 'List<Long>'.");
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
