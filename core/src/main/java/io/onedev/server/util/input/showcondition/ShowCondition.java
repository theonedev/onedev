package io.onedev.server.util.input.showcondition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.OneContext;
import io.onedev.server.util.editable.annotation.ChoiceProvider;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.OmitName;
import io.onedev.server.util.input.Input;

@Editable
public class ShowCondition implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String inputName;
	
	private ValueMatcher valueMatcher = new ValueIsOneOf();
	
	@Editable(order=100, name="resource.input.whenInput")
	@ChoiceProvider("getNameChoices")
	@NotEmpty
	public String getInputName() {
		return inputName;
	}

	public void setInputName(String inputName) {
		this.inputName = inputName;
	}

	@Editable(order=200)
	@OmitName
	@NotNull
	public ValueMatcher getValueMatcher() {
		return valueMatcher;
	}

	public void setValueMatcher(ValueMatcher valueMatcher) {
		this.valueMatcher = valueMatcher;
	}

	@SuppressWarnings("unused")
	private static List<String> getNameChoices() {
		return new ArrayList<>(OneContext.get().getInputContext().getInputNames());
	}
	
	public boolean isVisible() {
		Input input = OneContext.get().getInputContext().getInput(getInputName());
		String inputValue = input.toString(OneContext.get().getEditContext().getOnScreenValue(getInputName()));
		return getValueMatcher().matches(inputValue);
	}
	
}
