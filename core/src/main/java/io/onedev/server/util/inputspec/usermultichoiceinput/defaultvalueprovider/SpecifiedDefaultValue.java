package io.onedev.server.util.inputspec.usermultichoiceinput.defaultvalueprovider;

import java.util.List;

import javax.validation.Validator;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.OmitName;
import io.onedev.server.util.editable.annotation.UserChoice;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.util.inputspec.userchoiceprovider.ChoiceProvider;

@Editable(order=100, name="Specified default value")
public class SpecifiedDefaultValue implements DefaultValueProvider {

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
	private static List<UserFacade> getValueChoices() {
		ChoiceProvider choiceProvider = (ChoiceProvider) OneContext.get().getEditContext(1).getInputValue("choiceProvider");
		if (choiceProvider != null && OneDev.getInstance(Validator.class).validate(choiceProvider).isEmpty())
			return choiceProvider.getChoices(true);
		else
			return Lists.newArrayList();
	}
	
}
