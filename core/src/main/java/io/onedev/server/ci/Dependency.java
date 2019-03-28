package io.onedev.server.ci;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.ci.jobparam.JobParam;
import io.onedev.server.util.OneContext;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.PathPatterns;
import io.onedev.server.web.page.project.blob.render.renderers.cispec.dependencies.DependencyEditPanel;

@Editable
public class Dependency implements Serializable {

	private static final long serialVersionUID = 1L;

	private String jobName;
	
	private List<JobParam> params = new ArrayList<>();
	
	private String retrieveArtifacts;

	@Editable(order=100)
	@ChoiceProvider("getJobChoices")
	@NotEmpty
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	@Editable(order=200, description="Specify parameters of the dependency job. Dependency is satisfied only when "
			+ "there are builds of the job with same set of parameters specified here")
	public List<JobParam> getParams() {
		return params;
	}

	public void setParams(List<JobParam> params) {
		this.params = params;
	}

	@Editable(order=300, description="Optionally specify space-separated artifacts to retrieve into job workspace. "
			+ "Use * or ? for wildcard match")
	@PathPatterns
	public String getRetrieveArtifacts() {
		return retrieveArtifacts;
	}

	public void setRetrieveArtifacts(String retrieveArtifacts) {
		this.retrieveArtifacts = retrieveArtifacts;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getJobChoices() {
		DependencyEditPanel editor = OneContext.get().getComponent().findParent(DependencyEditPanel.class);
		List<String> choices = new ArrayList<>();
		Job belongingJob = editor.getBelongingJob();
		for (Job job: editor.getEditingCISpec().getJobs()) {
			choices.add(job.getName());
		}
		choices.remove(belongingJob.getName());
		return choices;
	}
	
}
