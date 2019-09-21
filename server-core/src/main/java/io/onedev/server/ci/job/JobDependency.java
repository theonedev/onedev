package io.onedev.server.ci.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.CISpecAware;
import io.onedev.server.ci.job.param.JobParam;
import io.onedev.server.ci.job.paramspec.ParamSpec;
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
	
	private List<JobParam> jobParams = new ArrayList<>();
	
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

	@Editable(order=200)
	@ParamSpecProvider("getParamSpecs")
	@OmitName
	public List<JobParam> getJobParams() {
		return jobParams;
	}

	public void setJobParams(List<JobParam> jobParams) {
		this.jobParams = jobParams;
	}
	
	@Editable(order=300, name="Artifacts to Retrieve", description="Optionally specify artifacts to retrieve "
			+ "from the dependency into job workspace. Leave empty to do not retrieve any artifacts. "
			+ "<b>Note:</b> Type '@' to start inserting variable")
	@Interpolative(variableSuggester="suggestVariables")
	@Patterns
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
		List<String> choices = new ArrayList<>();
		Component component = ComponentContext.get().getComponent();
		CISpecAware ciSpecAware = WicketUtils.findInnermost(component, CISpecAware.class);
		if (ciSpecAware != null) {
			CISpec ciSpec = ciSpecAware.getCISpec();
			if (ciSpec != null) {
				for (Job eachJob: ciSpec.getJobs()) {
					if (eachJob.getName() != null)
						choices.add(eachJob.getName());
				}
			}
			JobAware jobAware = WicketUtils.findInnermost(component, JobAware.class);
			if (jobAware != null) {
				Job job = jobAware.getJob();
				if (job != null)
					choices.remove(job.getName());
			}
		}
		return choices;
	}
	
	@SuppressWarnings("unused")
	private static List<ParamSpec> getParamSpecs() {
		String jobName = (String) EditContext.get().getInputValue("jobName");
		if (jobName != null) {
			Component component = ComponentContext.get().getComponent();
			CISpecAware ciSpecAware = WicketUtils.findInnermost(component, CISpecAware.class);
			if (ciSpecAware != null) {
				CISpec ciSpec = ciSpecAware.getCISpec();
				if (ciSpec != null) {
					Job job = ciSpec.getJobMap().get(jobName);
					if (job != null)
						return job.getParamSpecs();
				}
			}
		} 
		return new ArrayList<>();
	}
	
}
