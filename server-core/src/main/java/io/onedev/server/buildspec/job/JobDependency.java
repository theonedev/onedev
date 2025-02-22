package io.onedev.server.buildspec.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.annotation.ParamSpecProvider;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.annotation.ShowCondition;
import io.onedev.server.annotation.VariableOption;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.param.instance.ParamInstances;
import io.onedev.server.buildspec.param.instance.ParamMap;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.EditContext;
import io.onedev.server.web.editable.BeanEditor;

@Editable
public class JobDependency implements Serializable {

	private static final long serialVersionUID = 1L;

	private String jobName;
	
	private boolean requireSuccessful = true;
	
	private List<ParamInstances> paramMatrix = new ArrayList<>();
	
	private List<ParamMap> excludeParamMaps = new ArrayList<>();
	
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

	@Editable(order=200)
	@ParamSpecProvider("getParamSpecs")
	@VariableOption(withBuildVersion=false, withDynamicVariables=false)
	@OmitName
	@Valid
	public List<ParamInstances> getParamMatrix() {
		return paramMatrix;
	}

	public void setParamMatrix(List<ParamInstances> paramMatrix) {
		this.paramMatrix = paramMatrix;
	}

	@Editable(order=300, name="Exclude Param Combos")
	@ShowCondition("isExcludeParamMapsVisible")
	public List<ParamMap> getExcludeParamMaps() {
		return excludeParamMaps;
	}

	public void setExcludeParamMaps(List<ParamMap> excludeParamMaps) {
		this.excludeParamMaps = excludeParamMaps;
	}

	@SuppressWarnings({ "unused", "unchecked" })
	private static boolean isExcludeParamMapsVisible() {
		var componentContext = ComponentContext.get();
		if (componentContext != null && componentContext.getComponent().findParent(BeanEditor.class) != null) {
			return !getParamSpecs().isEmpty();
		} else {
			var excludeParamMaps = (List<ParamMap>) EditContext.get().getInputValue("excludeParamMaps");
			return !excludeParamMaps.isEmpty();
		}
	}
	
	public static List<ParamSpec> getParamSpecs() {
		var buildSpec = BuildSpec.get();
		if (buildSpec != null) {
			String jobName = (String) EditContext.get().getInputValue("jobName");
			if (jobName != null) {
				Job job = buildSpec.getJobMap().get(jobName);
				if (job != null)
					return job.getParamSpecs();
			}
		}
		return new ArrayList<>();
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
	
}
