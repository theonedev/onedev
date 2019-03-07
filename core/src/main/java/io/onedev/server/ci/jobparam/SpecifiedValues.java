package io.onedev.server.ci.jobparam;

import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Splitter;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Multiline;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable(order=100, name="Use specified values")
public class SpecifiedValues implements ValueProvider {

	private static final long serialVersionUID = 1L;

	private String value;
	
	@Editable(name="Values", description="Specify values of the parameter, with each line representing a separate value")
	@OmitName
	@NotEmpty
	@Multiline
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<String> getValues() {
		return Splitter.on("\n").trimResults().omitEmptyStrings().splitToList(value);
	}
	
}
