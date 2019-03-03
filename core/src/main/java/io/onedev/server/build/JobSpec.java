package io.onedev.server.build;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Horizontal;

@Editable
@Horizontal
public class JobSpec implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private List<DependencySpec> dependencies = new ArrayList<>();
	
	private List<InputSpec> params = new ArrayList<>();
	
	private List<StepSpec> steps = new ArrayList<>();
	
	@Editable(order=100)
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200)
	public List<InputSpec> getParams() {
		return params;
	}

	public void setParams(List<InputSpec> params) {
		this.params = params;
	}

	@Editable(order=300)
	public List<StepSpec> getSteps() {
		return steps;
	}

	public void setSteps(List<StepSpec> steps) {
		this.steps = steps;
	}
	
	@Editable(order=400)
	public List<DependencySpec> getDependencies() {
		return dependencies;
	}

	public void setDependencies(List<DependencySpec> dependencies) {
		this.dependencies = dependencies;
	}

}
