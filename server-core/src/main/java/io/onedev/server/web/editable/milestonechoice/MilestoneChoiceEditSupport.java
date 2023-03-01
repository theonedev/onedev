package io.onedev.server.web.editable.milestonechoice;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.util.StringUtils;

import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;
import io.onedev.server.annotation.MilestoneChoice;

@SuppressWarnings("serial")
public class MilestoneChoiceEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
        Method propertyGetter = descriptor.getPropertyGetter();
        MilestoneChoice milestoneChoice = propertyGetter.getAnnotation(MilestoneChoice.class);
        if (milestoneChoice != null) {
        	if (List.class.isAssignableFrom(propertyGetter.getReturnType()) 
        			&& ReflectionUtils.getCollectionElementClass(propertyGetter.getGenericReturnType()) == String.class) {
        		return new PropertyContext<List<String>>(descriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<List<String>> model) {
						return new PropertyViewer(componentId, descriptor) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
						        List<String> milestoneNames = model.getObject();
						        if (milestoneNames != null && !milestoneNames.isEmpty()) {
						            return new Label(id, StringUtils.join(milestoneNames, ", " ));
						        } else {
									return new EmptyValueLabel(id) {

										@Override
										protected AnnotatedElement getElement() {
											return propertyDescriptor.getPropertyGetter();
										}
										
									};
						        }
							}
							
						};
					}

					@Override
					public PropertyEditor<List<String>> renderForEdit(String componentId, IModel<List<String>> model) {
						return new MilestoneMultiChoiceEditor(componentId, descriptor, model);
					}
        			
        		};
        	} else if (propertyGetter.getReturnType() == String.class) {
        		return new PropertyContext<String>(descriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<String> model) {
						return new PropertyViewer(componentId, descriptor) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
								if (model.getObject() != null) {
						            return new Label(id, model.getObject());
						        } else {
									return new EmptyValueLabel(id) {

										@Override
										protected AnnotatedElement getElement() {
											return propertyDescriptor.getPropertyGetter();
										}
										
									};
						        }
							}
							
						};
					}

					@Override
					public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
						return new MilestoneSingleChoiceEditor(componentId, descriptor, model);
					}
        			
        		};
        	} else {
        		throw new RuntimeException("Annotation 'MilestoneChoice' should be applied to property with type 'String' or 'List<String>'");
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
