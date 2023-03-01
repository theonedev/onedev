package io.onedev.server.web.editable;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.google.common.collect.Lists;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.behavior.InterpolativeAssistBehavior;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.Markdown;
import io.onedev.server.web.editable.markdown.MarkdownPropertyEditor;
import io.onedev.server.web.editable.string.StringPropertyEditor;
import io.onedev.server.web.editable.string.StringPropertyViewer;

@SuppressWarnings("serial")
public class InterpolativeEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Method propertyGetter = descriptor.getPropertyGetter();
		Interpolative interpolative = propertyGetter.getAnnotation(Interpolative.class);
        if (interpolative != null) {
        	if (propertyGetter.getReturnType() == String.class) {
        		return new PropertyContext<String>(descriptor) {

    				@Override
    				public PropertyViewer renderForView(String componentId, IModel<String> model) {
    					if (descriptor.getPropertyGetter().getAnnotation(Markdown.class) != null) {
    						return new PropertyViewer(componentId, descriptor) {

    							@Override
    							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
    								return new MarkdownViewer(id, Model.of(model.getObject()), null);
    							}
    							
    						};
    					} else {
    						return new StringPropertyViewer(componentId, descriptor, model.getObject());
    					}
    				}

    				@Override
    				public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
    					InterpolativeAssistBehavior inputAssist = new InterpolativeAssistBehavior() {

							@SuppressWarnings("unchecked")
							@Override
							protected List<InputSuggestion> suggestVariables(String matchWith) {
								String suggestionMethod = interpolative.variableSuggester();
								if (suggestionMethod.length() != 0) {
									return (List<InputSuggestion>) ReflectionUtils.invokeStaticMethod(
											descriptor.getBeanClass(), suggestionMethod, new Object[] {matchWith});
								} else {
									return Lists.newArrayList();
								}
							}
							
							@SuppressWarnings("unchecked")
							@Override
							protected List<InputSuggestion> suggestLiterals(String matchWith) {
								String suggestionMethod = interpolative.literalSuggester();
								if (suggestionMethod.length() != 0) {
									return (List<InputSuggestion>) ReflectionUtils.invokeStaticMethod(
											descriptor.getBeanClass(), suggestionMethod, new Object[] {matchWith});
								} else {
									return Lists.newArrayList();
								}
							}
							
						};
    					if (descriptor.getPropertyGetter().getAnnotation(Markdown.class) != null) {
    						return new MarkdownPropertyEditor(componentId, descriptor, model) {

								@Override
								protected List<Behavior> getInputBehaviors() {
									return Lists.newArrayList(inputAssist);
								}
    							
    						};
    					} else {
    						return new StringPropertyEditor(componentId, descriptor, model).setInputAssist(inputAssist);
    					}
    				}
        			
        		};
        	} else if (propertyGetter.getReturnType() == List.class) {
        		return null;
        	} else {
	    		throw new RuntimeException("Annotation 'Interpolative' should be applied to property "
	    				+ "of type 'String' or 'List<String>'");
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
