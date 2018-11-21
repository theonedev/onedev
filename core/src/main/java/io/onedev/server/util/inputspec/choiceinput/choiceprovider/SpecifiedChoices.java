package io.onedev.server.util.inputspec.choiceinput.choiceprovider;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.validation.constraints.Size;

import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable(order=100, name="Use specified choices")
public class SpecifiedChoices extends ChoiceProvider {

	private static final long serialVersionUID = 1L;

	private List<Choice> choices = new ArrayList<>();

	@Editable(name="Specified choices")
	@Size(min=2, message="At least two choices need to be specified")
	@OmitName
	public List<Choice> getChoices() {
		return choices;
	}

	public void setChoices(List<Choice> choices) {
		this.choices = choices;
	}

	@Override
	public Map<String, String> getChoices(boolean allPossible) {
		Map<String, String> choices = new LinkedHashMap<>();
		for (Choice choice: getChoices()) 
			choices.put(choice.getValue(), choice.getColor());
		return choices;
	}
	
	public List<String> getChoiceValues() {
		return choices.stream().map(it->it.getValue()).collect(Collectors.toList());
	}
	
	@Nullable
	public static SpecifiedChoices of(@Nullable InputSpec inputSpec) {
		if (inputSpec instanceof ChoiceInput) { 
			ChoiceInput choiceInput = (ChoiceInput) inputSpec;
			if (choiceInput.getChoiceProvider() instanceof SpecifiedChoices) 
				return (SpecifiedChoices) choiceInput.getChoiceProvider();
		}
		return null;
	}
	
}
