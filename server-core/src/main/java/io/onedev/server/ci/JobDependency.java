package io.onedev.server.ci;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.ci.job.Job;
import io.onedev.server.ci.job.JobAware;
import io.onedev.server.ci.job.param.JobParam;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
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
	
	@Editable(order=100)
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
			+ "from the dependency into job workspace. Leave empty to do not retrieve any artifacts")
	@Patterns
	@NameOfEmptyValue("Do not retrieve")
	public String getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(String artifacts) {
		this.artifacts = artifacts;
	}

	@SuppressWarnings("unused")
	private static List<String> getJobChoices() {
		List<String> choices = new ArrayList<>();
		Component component = OneContext.get().getComponent();
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
	private static List<InputSpec> getParamSpecs() {
		String jobName = (String) OneContext.get().getEditContext().getInputValue("jobName");
		if (jobName != null) {
			Component component = OneContext.get().getComponent();
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
