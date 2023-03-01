package io.onedev.server.buildspec.param.spec.choiceparam.defaultvalueprovider;

import io.onedev.server.OneDev;
import io.onedev.server.buildspecmodel.inputspec.choiceinput.choiceprovider.ChoiceProvider;
import io.onedev.server.util.EditContext;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;

import javax.validation.Validator;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Editable(order=100, name="Use specified default value")
public class SpecifiedDefaultValue implements DefaultValueProvider {

	private static final long serialVersionUID = 1L;

	private String value;

	@Editable(name="Literal default value")
	@io.onedev.server.annotation.ChoiceProvider("getValueChoices")
	@NotEmpty
	@OmitName
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String getDefaultValue() {
		return getValue();
	}

	@SuppressWarnings("unused")
	private static List<String> getValueChoices() {
		ChoiceProvider choiceProvider = (ChoiceProvider) EditContext.get(1).getInputValue("choiceProvider");
		if (choiceProvider != null && OneDev.getInstance(Validator.class).validate(choiceProvider).isEmpty())
			return new ArrayList<>(choiceProvider.getChoices(true).keySet());
		else
			return new ArrayList<>();
	}

}
