package io.onedev.server.buildspecmodel.inputspec.showcondition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import javax.validation.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.server.buildspecmodel.inputspec.InputContext;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;
import io.onedev.server.util.EditContext;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;

@Editable
public class ShowCondition implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(ShowCondition.class);
	
	private String inputName;
	
	private ValueMatcher valueMatcher = new ValueIsOneOf();
	
	@Editable(order=100, name="When")
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
	@NotNull(message="may not be empty")
	public ValueMatcher getValueMatcher() {
		return valueMatcher;
	}

	public void setValueMatcher(ValueMatcher valueMatcher) {
		this.valueMatcher = valueMatcher;
	}

	@SuppressWarnings("unused")
	private static List<String> getNameChoices() {
		return new ArrayList<>(Preconditions.checkNotNull(InputContext.get().getInputNames()));
	}
	
	public boolean isVisible() {
		InputSpec inputSpec = Preconditions.checkNotNull(InputContext.get()).getInputSpec(getInputName());
		if (inputSpec != null) {
			Object inputValue = EditContext.get().getInputValue(getInputName());
			return getValueMatcher().matches(inputSpec.convertToStrings(inputValue));
		} else {
			logger.error("Unable to find input spec: " + getInputName());
			return false;
		}
	}
	
}
