package io.onedev.server.buildspec.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import javax.validation.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.BuildSpecAware;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.buildspec.param.supply.ParamSupply;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.EditContext;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.ParamSpecProvider;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.editable.annotation.VariableOption;
import io.onedev.server.web.util.WicketUtils;

@Editable
public class JobDependency implements Serializable {

	private static final long serialVersionUID = 1L;

	private String jobName;
	
	private boolean requireSuccessful = true;
	
	private List<ParamSupply> jobParams = new ArrayList<>();
	
	private String artifacts = "**";
	
	private String destinationPath;
	
	// change Named("jobName") also if change name of this property 
	@Editable(order=100, name="Job")
	@ChoiceProvider("getJobChoices")
	@NotEmpty
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	@Editable(order=150, description="Whether or not to require this dependency to be successful")
	public boolean isRequireSuccessful() {
		return requireSuccessful;
	}

	public void setRequireSuccessful(boolean requireSuccessful) {
		this.requireSuccessful = requireSuccessful;
	}

	@Editable(order=200, name="Job Parameters")
	@ParamSpecProvider("getParamSpecs")
	@VariableOption(withBuildVersion=false, withDynamicVariables=false)
	@OmitName
	public List<ParamSupply> getJobParams() {
		return jobParams;
	}

	public void setJobParams(List<ParamSupply> jobParams) {
		this.jobParams = jobParams;
	}
	
	@Editable(order=300, name="Artifacts to Retrieve", placeholder="Do not retrieve", description=""
			+ "Optionally specify artifacts to retrieve from the dependency into "
			+ "<a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. "
			+ "Only published artifacts (via artifact publish step) can be retrieved. Leave empty to not "
			+ "retrieve any artifacts")
	@Interpolative(variableSuggester="suggestVariables")
	@Patterns(path=true)
	public String getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(String artifacts) {
		this.artifacts = artifacts;
	}

	@Editable(order=400, placeholder="Job workspace", description=""
			+ "Optionally specify a path relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a> "
			+ "to put retrieved artifacts. Leave empty to use job workspace itself")
	@Interpolative(variableSuggester="suggestVariables")
	public String getDestinationPath() {
		return destinationPath;
	}

	public void setDestinationPath(String destinationPath) {
		this.destinationPath = destinationPath;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, false, false);
	}
	
	@SuppressWarnings("unused")
	private static List<String> getJobChoices() {
		return Job.getChoices();
	}
	
	@SuppressWarnings("unused")
	private static List<ParamSpec> getParamSpecs() {
		String jobName = (String) EditContext.get().getInputValue("jobName");
		if (jobName != null) {
			Component component = ComponentContext.get().getComponent();
			BuildSpecAware buildSpecAware = WicketUtils.findInnermost(component, BuildSpecAware.class);
			if (buildSpecAware != null) {
				BuildSpec buildSpec = buildSpecAware.getBuildSpec();
				if (buildSpec != null) {
					Job job = buildSpec.getJobMap().get(jobName);
					if (job != null)
						return job.getParamSpecs();
				}
			}
		} 
		return new ArrayList<>();
	}

}
