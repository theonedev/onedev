package io.onedev.server.model.support.inputspec.showcondition;

import java.util.List;

import javax.validation.constraints.Size;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.onedev.server.model.support.inputspec.InputContext;
import io.onedev.server.model.support.inputspec.InputSpec;
import io.onedev.server.util.EditContext;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable(order=200, name="does not have any value of")
public class ValueIsNotAnyOf implements ValueMatcher {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(ValueIsNotAnyOf.class);

	private List<String> values;

	@Editable
	@ChoiceProvider("getValueChoices")
	@OmitName
	@Size(min=1, message="At least one value needs to be specified")
	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	@SuppressWarnings("unused")
	private static List<String> getValueChoices() {
		// Access on-screen value of ShowCondition.fiedName
		String inputName = (String) EditContext.get(1).getInputValue("inputName");
		if (inputName != null) {
			InputSpec inputSpec = Preconditions.checkNotNull(InputContext.get()).getInputSpec(inputName);
			if (inputSpec != null) 
				return inputSpec.getPossibleValues();
			else
				logger.error("Unable to find input spec: " + inputName);
		}
		return Lists.newArrayList();
	}

	@Override
	public boolean matches(List<String> values) {
		return !CollectionUtils.containsAny(getValues(), values);
	}
	
}
