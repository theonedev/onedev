package io.onedev.server.model.support.inputspec.userchoiceinput.defaultmultivalueprovider;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Validator;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.model.User;
import io.onedev.server.model.support.inputspec.userchoiceinput.choiceprovider.ChoiceProvider;
import io.onedev.server.util.EditContext;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.UserChoice;

@Editable(order=100, name="Use specified default value")
public class SpecifiedDefaultMultiValue implements DefaultMultiValueProvider {

	private static final long serialVersionUID = 1L;

	private List<String> value;

	@Editable(name="Literal default value")
	@UserChoice("getValueChoices")
	@NotEmpty
	@OmitName
	public List<String> getValue() {
		return value;
	}

	public void setValue(List<String> value) {
		this.value = value;
	}

	@Override
	public List<String> getDefaultValue() {
		return getValue();
	}

	@SuppressWarnings("unused")
	private static List<User> getValueChoices() {
		ChoiceProvider choiceProvider = (ChoiceProvider) EditContext.get(1).getInputValue("choiceProvider");
		if (choiceProvider != null && OneDev.getInstance(Validator.class).validate(choiceProvider).isEmpty()) 
			return choiceProvider.getChoices(true);
		else
			return new ArrayList<>();
	}
	
}
