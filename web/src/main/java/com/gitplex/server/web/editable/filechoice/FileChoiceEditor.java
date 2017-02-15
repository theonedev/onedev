package com.gitplex.server.web.editable.filechoice;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.gitplex.codeassist.InputCompletion;
import com.gitplex.codeassist.InputStatus;
import com.gitplex.codeassist.InputSuggestion;
import com.gitplex.jsymbol.Range;
import com.gitplex.server.util.ReflectionUtils;
import com.gitplex.server.util.editable.annotation.FileChoice;
import com.gitplex.server.web.behavior.inputassist.InputAssistBehavior;
import com.gitplex.server.web.editable.ErrorContext;
import com.gitplex.server.web.editable.PathSegment;
import com.gitplex.server.web.editable.PropertyDescriptor;
import com.gitplex.server.web.editable.PropertyEditor;
import com.gitplex.server.web.util.SuggestionUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class FileChoiceEditor extends PropertyEditor<String> {

	private final List<String> choices = new ArrayList<>();
	
	private TextField<String> input;
	
	@SuppressWarnings("unchecked")
	public FileChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
		
		FileChoice fileChoice = propertyDescriptor.getPropertyGetter().getAnnotation(FileChoice.class);
		Preconditions.checkNotNull(fileChoice);
		for (String each: (List<String>)ReflectionUtils
				.invokeStaticMethod(propertyDescriptor.getBeanClass(), fileChoice.value())) {
			choices.add(each);
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		input = new TextField<String>("input", Model.of(getModelObject()));
		
		input.add(new InputAssistBehavior() {
			
			@Override
			protected List<InputCompletion> getSuggestions(InputStatus inputStatus, int count) {
				List<InputCompletion> completions = new ArrayList<>();
				for (InputSuggestion suggestion: SuggestionUtils.suggestPath(choices, 
						inputStatus.getContentBeforeCaret().trim(), count)) {
					int caret = suggestion.getCaret();
					if (caret == -1)
						caret = suggestion.getContent().length();
					InputCompletion completion = new InputCompletion(0, inputStatus.getContent().length(), 
							suggestion.getContent(), caret, suggestion.getLabel(), 
							null, suggestion.getMatchRange());
					completions.add(completion);
				}
				return completions;
			}
			
			@Override
			protected List<String> getHints(InputStatus inputStatus) {
				return Lists.newArrayList("Use * to match any string in the path");
			}

			@Override
			protected List<Range> getErrors(String inputContent) {
				return null;
			}
			
			@Override
			protected int getAnchor(String content) {
				for (int i=0; i<content.length(); i++) {
					if (!Character.isWhitespace(content.charAt(i)))
						return i;
				}
				return content.length();
			}
			
		});
		
        add(input);
    }

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return null;
	}

	@Override
	protected String convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}

}
