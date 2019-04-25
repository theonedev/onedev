package io.onedev.server.ci.job.param;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class JobParam implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String INVALID_CHARS_MESSAGE = "param name should start with letter and can only consist of alphanumeric and underscore characters";
	
	private String name;
	
	private ValueProvider valueProvider = new SpecifiedValues();
	
	@Editable(order=100)
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200, name="Values", description="Specify parameter values here. Multiple values tell "
			+ "the job to run multiple times, each time with one value")
	@NotNull
	public ValueProvider getValueProvider() {
		return valueProvider;
	}

	public void setValueProvider(ValueProvider valueProvider) {
		this.valueProvider = valueProvider;
	}

}
