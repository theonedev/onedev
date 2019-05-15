package io.onedev.server.web.editable.job.paramspec;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class ParamSpecBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private InputSpec paramSpec;

	@Editable(name="Type", order=100)
	@NotNull(message="may not be empty")
	public InputSpec getParamSpec() {
		return paramSpec;
	}

	public void setParamSpec(InputSpec paramSpec) {
		this.paramSpec = paramSpec;
	}

}
