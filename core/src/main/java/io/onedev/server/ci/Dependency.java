package io.onedev.server.ci;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Preconditions;

import io.onedev.server.ci.job.Job;
import io.onedev.server.ci.job.param.JobParam;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.ParamSpecProvider;
import io.onedev.server.web.page.project.blob.render.renderers.cispec.CISpecAware;
import io.onedev.server.web.page.project.blob.render.renderers.cispec.job.dependency.DependencyEditPanel;
import io.onedev.server.web.util.WicketUtils;

@Editable
public class Dependency implements Serializable {

	private static final long serialVersionUID = 1L;

	private String jobName;
	
	private List<JobParam> jobParams = new ArrayList<>();
	
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
	
	@SuppressWarnings("unused")
	private static List<String> getJobChoices() {
		DependencyEditPanel editor = OneContext.get().getComponent().findParent(DependencyEditPanel.class);
		Preconditions.checkNotNull(editor);
		List<String> choices = new ArrayList<>();
		Job belongingJob = editor.getJob();
		for (Job job: editor.getCISpec().getJobs()) {
			if (job.getName() != null)
				choices.add(job.getName());
		}
		choices.remove(belongingJob.getName());
		return choices;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSpec> getParamSpecs() {
		String jobName = (String) OneContext.get().getEditContext().getInputValue("jobName");
		if (jobName != null) {
			Component component = OneContext.get().getComponent();
			CISpecAware ciSpecAware = Preconditions.checkNotNull(WicketUtils.findInnermost(component, CISpecAware.class));
			CISpec ciSpec = Preconditions.checkNotNull(ciSpecAware.getCISpec());
			Job job = Preconditions.checkNotNull(ciSpec.getJobMap().get(jobName));
			return job.getParamSpecs();
		} else {
			return new ArrayList<>();
		}
	}
	
}
