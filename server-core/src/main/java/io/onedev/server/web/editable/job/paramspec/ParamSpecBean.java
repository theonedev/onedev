package io.onedev.server.web.editable.job.paramspec;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.buildspec.job.paramspec.ParamSpec;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class ParamSpecBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private ParamSpec paramSpec;

	// change Named("paramSpec") also if change name of this property 
	@Editable(name="Type", order=100)
	@NotNull(message="may not be empty")
	public ParamSpec getParamSpec() {
		return paramSpec;
	}

	public void setParamSpec(ParamSpec paramSpec) {
		this.paramSpec = paramSpec;
	}

}
