package io.onedev.server.util.inputspec.showcondition;

import java.util.List;

import javax.validation.constraints.Size;

import com.google.common.collect.Lists;

import io.onedev.server.util.OneContext;
import io.onedev.server.util.editable.annotation.ChoiceProvider;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.OmitName;

@Editable(order=100, name="has value of")
public class ValueIsOneOf implements ValueMatcher {

	private static final long serialVersionUID = 1L;
	
	private List<String> values;

	@Editable
	@ChoiceProvider("getValueChoices")
	@OmitName
	@Size(min=1, max=100)
	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	@SuppressWarnings("unused")
	private static List<String> getValueChoices() {
		// Access on-screen value of ShowCondition.fiedName
		String inputName = (String) OneContext.get().getEditContext(1).getOnScreenValue("inputName");
		if (inputName != null)
			return OneContext.get().getInputContext().getInput(inputName).getPossibleValues();
		else
			return Lists.newArrayList();
	}

	@Override
	public boolean matches(String value) {
		return getValues().contains(value);
	}
	
}
