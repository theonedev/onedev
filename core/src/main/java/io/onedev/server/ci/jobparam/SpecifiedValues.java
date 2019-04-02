package io.onedev.server.ci.jobparam;

import java.util.List;

import javax.validation.constraints.Size;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable(order=100, name="Use specified values")
public class SpecifiedValues implements ValueProvider {

	private static final long serialVersionUID = 1L;

	private List<String> values;
	
	@Editable(name="Values", description="Specify values of the parameter, with each line representing a separate value")
	@OmitName
	@Size(min=1, message="At least one value needs to be specified")
	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

}
