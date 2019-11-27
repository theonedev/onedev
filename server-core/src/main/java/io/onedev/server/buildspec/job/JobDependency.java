package io.onedev.server.buildspec.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.BuildSpecAware;
import io.onedev.server.buildspec.job.paramspec.ParamSpec;
import io.onedev.server.buildspec.job.paramsupply.ParamSupply;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.EditContext;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.ParamSpecProvider;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.WicketUtils;

@Editable
public class JobDependency implements Serializable {

	private static final long serialVersionUID = 1L;

	private String jobName;
	
	private boolean requireSuccessful = true;
	
	private List<ParamSupply> jobParams = new ArrayList<>();
	
	private String artifacts = "**";
	
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
	@OmitName
	public List<ParamSupply> getJobParams() {
		return jobParams;
	}

	public void setJobParams(List<ParamSupply> jobParams) {
		this.jobParams = jobParams;
	}
	
	@Editable(order=300, name="Artifacts to Retrieve", description="Optionally specify artifacts to retrieve "
			+ "from the dependency into job workspace. Leave empty to do not retrieve any artifacts. "
			+ "<b>Note:</b> Type <tt>@</tt> to <a href='https://github.com/theonedev/onedev/wiki/Variable-Substitution' tabindex='-1'>insert variable</a>, use <tt>\\</tt> to escape normal occurrences of <tt>@</tt> or <tt>\\</tt>")
	@Interpolative(variableSuggester="suggestVariables")
	@Patterns(interpolative = true)
	@NameOfEmptyValue("Do not retrieve")
	public String getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(String artifacts) {
		this.artifacts = artifacts;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return Job.suggestVariables(matchWith);
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
