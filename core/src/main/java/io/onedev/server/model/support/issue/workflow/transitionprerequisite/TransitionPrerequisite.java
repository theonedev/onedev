package io.onedev.server.model.support.issue.workflow.transitionprerequisite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.OneContext;
import io.onedev.server.util.editable.annotation.ChoiceProvider;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.OmitName;

@Editable
public class TransitionPrerequisite implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String inputName;
	
	private ValueMatcher valueMatcher;
	
	@Editable(order=100, name="When field")
	@ChoiceProvider("getFieldNameChoices")
	@NotEmpty
	public String getInputName() {
		return inputName;
	}

	public void setInputName(String fieldName) {
		this.inputName = fieldName;
	}

	@Editable(order=200)
	@NotNull
	@OmitName
	public ValueMatcher getValueMatcher() {
		return valueMatcher;
	}

	public void setValueMatcher(ValueMatcher valueMatcher) {
		this.valueMatcher = valueMatcher;
	}

	@SuppressWarnings("unused")
	private static List<String> getFieldNameChoices() {
		return new ArrayList<>(OneContext.get().getInputContext().getInputNames());
	}
	
	public boolean matches(List<String> values) {
		if (values.isEmpty())
			return valueMatcher.matches(null);
		else if (values.size() == 1)
			return valueMatcher.matches(values.iterator().next());
		else 
			throw new IllegalStateException("Transition prerequisite should not be based on a multi-value input");
	}
	
}
