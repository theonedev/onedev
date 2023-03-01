package io.onedev.server.buildspec.step;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import javax.validation.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputCompletion;
import io.onedev.commons.codeassist.InputStatus;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.NamedElement;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.SuggestionProvider;

@Editable
public class StepTemplate implements NamedElement, Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String PROP_STEPS = "steps";
	
	private String name;
	
	private List<Step> steps = new ArrayList<>();
	
	private List<ParamSpec> paramSpecs = new ArrayList<>();
	
	@Editable(order=100)
	@SuggestionProvider("getNameSuggestions")
	@NotEmpty
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@SuppressWarnings("unused")
	private static List<InputCompletion> getNameSuggestions(InputStatus status) {
		BuildSpec buildSpec = BuildSpec.get();
		if (buildSpec != null) {
			List<String> candidates = new ArrayList<>(buildSpec.getStepTemplateMap().keySet());
			buildSpec.getStepTemplates().forEach(it->candidates.remove(it.getName()));
			return BuildSpec.suggestOverrides(candidates, status);
		}
		return new ArrayList<>();
	}
	
	@Editable(order=200, description="Steps will be executed serially on same node, sharing the same <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>")
	@Size(min=1, max=1000, message="At least one step needs to be defined")
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
