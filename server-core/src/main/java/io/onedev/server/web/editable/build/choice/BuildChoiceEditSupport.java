package io.onedev.server.web.editable.build.choice;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.unbescape.html.HtmlEscape;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyViewer;
import io.onedev.server.annotation.BuildChoice;

@SuppressWarnings("serial")
public class BuildChoiceEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
        Method propertyGetter = descriptor.getPropertyGetter();
        BuildChoice buildChoice = propertyGetter.getAnnotation(BuildChoice.class);
        if (buildChoice != null) {
        	if (List.class.isAssignableFrom(propertyGetter.getReturnType()) 
        			&& ReflectionUtils.getCollectionElementClass(propertyGetter.getGenericReturnType()) == Long.class) {
        		return new PropertyContext<List<Long>>(descriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<List<Long>> model) {
						return new PropertyViewer(componentId, descriptor) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
						        List<Long> buildIds = model.getObject();
						        if (buildIds != null && !buildIds.isEmpty()) {
						        	List<String> buildNumbers = new ArrayList<>();
						        	for (Long buildId: buildIds) {
						        		Build build = getBuildManager().get(buildId);
						        		if (build != null) 
						        			buildNumbers.add(HtmlEscape.escapeHtml5(getBuildNumber(build)));
						        		else
						        			buildNumbers.add("<i>Not Found</i>");
						        	}
						        	return new Label(id, StringUtils.join(buildNumbers, ", ")).setEscapeModelStrings(false);
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
								Long buildId = model.getObject();
								if (buildId != null) {
									Build build = getBuildManager().get(buildId);
									if (build != null) 
										return new Label(id, getBuildNumber(build));
									else 
										return new Label(id, "<i>Not Found</i>").setEscapeModelStrings(false);
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
	
	private String getBuildNumber(Build build) {
		if (Project.get() != null && Project.get().getForkRoot().equals(build.getNumberScope()))
			return "#" + build.getNumber();
		else
			return build.getFQN().toString();
	}
	
	private BuildManager getBuildManager() {
		return OneDev.getInstance(BuildManager.class);
	}

	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY;
	}

}
