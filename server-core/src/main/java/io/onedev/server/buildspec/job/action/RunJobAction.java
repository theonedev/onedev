package io.onedev.server.buildspec.job.action;

import static io.onedev.server.buildspec.param.ParamUtils.resolveParams;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotEmpty;

import org.apache.wicket.Component;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.annotation.ParamSpecProvider;
import io.onedev.server.annotation.ShowCondition;
import io.onedev.server.annotation.VariableOption;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.BuildSpecAware;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.param.ParamUtils;
import io.onedev.server.buildspec.param.instance.ParamInstances;
import io.onedev.server.buildspec.param.instance.ParamMap;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.job.JobService;
import io.onedev.server.model.Build;
import io.onedev.server.service.UserService;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.EditContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.util.WicketUtils;

@Editable(name="Run job", order=100)
public class RunJobAction extends PostBuildAction {

	private static final long serialVersionUID = 1L;

	private String jobName;
	
	private List<ParamInstances> paramMatrix = new ArrayList<>();
	
	private List<ParamMap> excludeParamMaps = new ArrayList<>();
	
	@Editable(order=900, name="Job")
	@ChoiceProvider("getJobChoices")
	@NotEmpty
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	@SuppressWarnings("unused")
	private static List<String> getJobChoices() {
		return Job.getChoices();
	}

	@Editable(order=1000)
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

	@Editable(order=1100, name="Exclude Param Combos")
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
	
	@Override
	public void execute(Build build) {
		for (var paramMap: resolveParams(build, build.getParamCombination(), getParamMatrix(), getExcludeParamMaps())) {
			JobService jobService = OneDev.getInstance(JobService.class);
			var userService = OneDev.getInstance(UserService.class);
			jobService.submit(userService.getSystem(), build.getProject(), build.getCommitId(), 
					getJobName(), paramMap, build.getRefName(), build.getRequest(), build.getIssue(), 
					"Triggered via post build action of job '" + build.getJobName() + "'");
		}
	}

	@Override
	public String getDescription() {
		return "Run job '" + jobName + "'";
	}

	@Override
	public void validateWith(BuildSpec buildSpec, Job job) {
		super.validateWith(buildSpec, job);
		
		Job jobToRun = buildSpec.getJobMap().get(jobName);
		if (jobToRun != null) {
			try {
				ParamUtils.validateParamMatrix(jobToRun.getParamSpecs(), paramMatrix);
				for (var exludeParamMap: excludeParamMaps)
					ParamUtils.validateParamMap(jobToRun.getParamSpecs(), exludeParamMap.getParams());
			} catch (ValidationException e) {
				String errorMessage = String.format("Error validating job parameters (job: %s, error message: %s)", 
						jobToRun.getName(), e.getMessage());
				throw new ValidationException(errorMessage);
			}
		} else {
			throw new ValidationException("Job not found (" + jobName + ")");
		}
	}

}
