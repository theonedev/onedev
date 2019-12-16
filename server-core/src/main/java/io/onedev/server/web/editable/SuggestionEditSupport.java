package io.onedev.server.web.editable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;

import io.onedev.commons.codeassist.InputCompletion;
import io.onedev.commons.codeassist.InputStatus;
import io.onedev.commons.utils.LinearRange;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.server.web.editable.annotation.SuggestionProvider;
import io.onedev.server.web.editable.string.StringPropertyEditor;
import io.onedev.server.web.editable.string.StringPropertyViewer;

@SuppressWarnings("serial")
public class SuggestionEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Method propertyGetter = descriptor.getPropertyGetter();
		SuggestionProvider suggestionProvider = propertyGetter.getAnnotation(SuggestionProvider.class);
        if (suggestionProvider != null) {
        	if (propertyGetter.getReturnType() == String.class) {
        		return new PropertyContext<String>(descriptor) {

    				@Override
    				public PropertyViewer renderForView(String componentId, IModel<String> model) {
    					return new StringPropertyViewer(componentId, descriptor, model.getObject());
    				}

    				@Override
    				public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
    		        	return new StringPropertyEditor(componentId, descriptor, model) {

    						@Override
    						protected InputAssistBehavior getInputAssistBehavior() {
    							return new InputAssistBehavior() {

									@SuppressWarnings("unchecked")
									@Override
									protected List<InputCompletion> getSuggestions(InputStatus inputStatus) {
										String suggestionMethod = suggestionProvider.value();
										return (List<InputCompletion>) ReflectionUtils.invokeStaticMethod(
												descriptor.getBeanClass(), 
												suggestionMethod, new Object[] {inputStatus});
									}

									@Override
									protected List<LinearRange> getErrors(String inputContent) {
										return new ArrayList<>();
									}

									@Override
									protected int getAnchor(String inputContent) {
										return 0;
									}
									
    							};
    						}
    		        		
    		        	};
    				}
        			
        		};
        	} else {
	    		throw new RuntimeException("Annotation 'SuggestionProvider' should be applied to property "
	    				+ "of type 'String'");
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
