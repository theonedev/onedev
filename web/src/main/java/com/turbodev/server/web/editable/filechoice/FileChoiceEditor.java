package com.turbodev.server.web.editable.filechoice;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.turbodev.codeassist.InputCompletion;
import com.turbodev.codeassist.InputStatus;
import com.turbodev.codeassist.InputSuggestion;
import com.turbodev.utils.Range;
import com.turbodev.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.turbodev.server.util.editable.annotation.FileChoice;
import com.turbodev.server.web.behavior.inputassist.InputAssistBehavior;
import com.turbodev.server.web.editable.ErrorContext;
import com.turbodev.server.web.editable.PathSegment;
import com.turbodev.server.web.editable.PropertyDescriptor;
import com.turbodev.server.web.editable.PropertyEditor;
import com.turbodev.server.web.util.SuggestionUtils;

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
			protected List<InputCompletion> getSuggestions(InputStatus inputStatus) {
				List<InputCompletion> completions = new ArrayList<>();
				for (InputSuggestion suggestion: SuggestionUtils.suggestPath(choices, 
						inputStatus.getContentBeforeCaret().trim())) {
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
