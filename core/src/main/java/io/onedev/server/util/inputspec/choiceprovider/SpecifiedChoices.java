package io.onedev.server.util.inputspec.choiceprovider;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.OmitName;
import io.onedev.utils.StringUtils;

@Editable(order=100, name="Use specified choices")
public class SpecifiedChoices implements ChoiceProvider {

	private static final long serialVersionUID = 1L;

	private String values;

	@Editable(name="Specified choices", description="Specify comma separated choices, for instance: choice1, choice2")
	@NotEmpty
	@OmitName
	public String getValues() {
		return values;
	}

	public void setValues(String values) {
		this.values = values;
	}

	@Override
	public List<String> getChoices(boolean allPossible) {
		if (getValues() != null)
			return StringUtils.splitAndTrim(getValues(), ",");
		else
			return new ArrayList<>();
	}

}
