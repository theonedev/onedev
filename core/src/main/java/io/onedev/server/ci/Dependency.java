package io.onedev.server.ci;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.ci.jobparam.JobParam;
import io.onedev.server.util.OneContext;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Multiline;
import io.onedev.server.web.page.project.blob.render.renderers.cispec.dependencies.DependencyEditPanel;

@Editable
public class Dependency implements Serializable {

	private static final long serialVersionUID = 1L;

	private String job;
	
	private List<JobParam> params = new ArrayList<>();
	
	private String artifacts;

	@Editable(order=100)
	@ChoiceProvider("getJobChoices")
	@NotEmpty
	public String getJob() {
		return job;
	}

	public void setJob(String job) {
		this.job = job;
	}

	@Editable(order=200, description="Specify parameters of the dependency job. Dependency is satisfied only when "
			+ "there are builds of the job with same set of parameters specified here")
	public List<JobParam> getParams() {
		return params;
	}

	public void setParams(List<JobParam> params) {
		this.params = params;
	}

	@Editable(order=300, description="Optionally specify space-separated artifact paths to retrieve. Path is relative to "
			+ "build artifacts directory. Use * or ? for wildcard match")
	@Multiline
	public String getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(String artifacts) {
		this.artifacts = artifacts;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getJobChoices() {
		DependencyEditPanel editor = OneContext.get().getComponent().findParent(DependencyEditPanel.class);
		List<String> choices = new ArrayList<>();
		Job belongingJob = editor.getBelongingJob();
		for (Job job: editor.getCISpec().getJobs()) {
			choices.add(job.getName());
		}
		choices.remove(belongingJob.getName());
		return choices;
	}
	
}
