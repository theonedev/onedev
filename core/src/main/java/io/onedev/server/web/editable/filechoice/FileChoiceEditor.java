package io.onedev.server.web.editable.filechoice;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.codeassist.InputCompletion;
import io.onedev.codeassist.InputStatus;
import io.onedev.codeassist.InputSuggestion;
import io.onedev.server.util.editable.annotation.FileChoice;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.util.SuggestionUtils;
import io.onedev.utils.Range;
import io.onedev.utils.ReflectionUtils;

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
		input.setLabel(Model.of(getPropertyDescriptor().getDisplayName(this)));
		
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
