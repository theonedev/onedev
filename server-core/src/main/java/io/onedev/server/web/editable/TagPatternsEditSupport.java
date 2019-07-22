package io.onedev.server.web.editable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;

import com.google.common.collect.Lists;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.server.model.Project;
import io.onedev.server.web.behavior.PatternSetAssistBehavior;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.server.web.editable.annotation.TagPatterns;
import io.onedev.server.web.editable.string.StringPropertyEditor;
import io.onedev.server.web.editable.string.StringPropertyViewer;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class TagPatternsEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Method propertyGetter = descriptor.getPropertyGetter();
        if (propertyGetter.getAnnotation(TagPatterns.class) != null) {
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
    							return new PatternSetAssistBehavior() {

									@Override
									protected List<InputSuggestion> suggest(String matchWith) {
										if (getPage() instanceof ProjectPage) {
											Project project = ((ProjectPage)getPage()).getProject();
											return SuggestionUtils.suggestTags(project, matchWith);
										} else {
											return new ArrayList<>();
										}
									}
    								
									@Override
									protected List<String> getHints(TerminalExpect terminalExpect) {
										return Lists.newArrayList("Use * or ? for wildcard match");
									}
									
    							};
    						}
    		        		
    		        	};
    				}
        			
        		};
        	} else {
	    		throw new RuntimeException("Annotation 'TagPatterns' should be applied to property "
	    				+ "with type 'List<String>' or 'String'");
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
