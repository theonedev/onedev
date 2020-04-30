package io.onedev.server.model.support.inputspec.choiceinput.choiceprovider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Size;

import io.onedev.server.buildspec.job.paramspec.ChoiceParam;
import io.onedev.server.model.support.inputspec.InputSpec;
import io.onedev.server.model.support.issue.fieldspec.ChoiceField;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable(order=100, name="Use specified choices")
@ClassValidating
public class SpecifiedChoices extends ChoiceProvider implements Validatable {

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
		if (inputSpec instanceof ChoiceField) { 
			ChoiceField choiceField = (ChoiceField) inputSpec;
			if (choiceField.getChoiceProvider() instanceof SpecifiedChoices) 
				return (SpecifiedChoices) choiceField.getChoiceProvider();
		} else if (inputSpec instanceof ChoiceParam) { 
			ChoiceParam choiceParam = (ChoiceParam) inputSpec;
			if (choiceParam.getChoiceProvider() instanceof SpecifiedChoices) 
				return (SpecifiedChoices) choiceParam.getChoiceProvider();
		} 
 
		return null;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		Set<String> existing = new HashSet<>();
		for (Choice choice: choices) {
			if (existing.contains(choice.getValue())) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("Duplicate choice: " + choice.getValue()).addConstraintViolation();
				return false;
			} else {
				existing.add(choice.getValue());
			}
		}
		return true;
	}
	
}
