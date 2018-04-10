package io.onedev.server.util.inputspec.choiceprovider;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Size;

import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.OmitName;

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
}
