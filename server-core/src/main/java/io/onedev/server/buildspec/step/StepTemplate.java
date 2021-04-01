package io.onedev.server.buildspec.step;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.buildspec.NamedElement;
import io.onedev.server.buildspec.job.paramspec.ParamSpec;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class StepTemplate implements NamedElement, Serializable {
	
	private static final long serialVersionUID = 1L;

	private String name;
	
	private List<Step> steps = new ArrayList<>();
	
	private List<ParamSpec> paramSpecs = new ArrayList<>();
	
	@Editable(order=100)
	@NotEmpty
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200, name="Steps to Execute")
	public List<Step> getSteps() {
		return steps;
	}

	public void setSteps(List<Step> steps) {
		this.steps = steps;
	}

	@Editable(order=300, name="Parameter Specs", description="Optionally define parameter specifications of the step template")
	@Valid
	public List<ParamSpec> getParamSpecs() {
		return paramSpecs;
	}

	public void setParamSpecs(List<ParamSpec> paramSpecs) {
		this.paramSpecs = paramSpecs;
	}

}
