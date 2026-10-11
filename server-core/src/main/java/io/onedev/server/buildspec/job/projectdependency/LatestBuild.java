package io.onedev.server.buildspec.job.projectdependency;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.service.BuildService;
import io.onedev.server.util.EditContext;

@Editable(order=100, name="Latest Build of Specified Job")
public class LatestBuild implements BuildProvider {

	@Serial
    private static final long serialVersionUID = 1L;
	
	private String jobName;
	
	private String refName;
	
	@Editable(order=100)
	@ChoiceProvider(value = "getJobChoices", tagsMode = true)
	@NotEmpty
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	@Editable(name="Ref Name", order=200, placeholder="Any ref", description="Optionally specify ref of above job, "
			+ "for instance <i>refs/heads/main</i>. Use * for wildcard match")
	public String getRefName() {
		return refName;
	}

	public void setRefName(String refName) {
		this.refName = refName;
	}

	@SuppressWarnings("unused")
	private static List<String> getJobChoices() {
		Project project = ProjectDependency.getInputProject(EditContext.get(1));
		List<String> jobNames = new ArrayList<>();
		if (project != null) {
			jobNames.addAll(OneDev.getInstance(BuildService.class).getJobNames(project));
			Collections.sort(jobNames);
		}
		return jobNames;
	}
	
	@Override
	public Build getBuild(Project project) {
		return OneDev.getInstance(BuildService.class).findLatest(project, jobName, refName);
	}

	@Override
	public String getDescription() {
		if (refName != null)
			return "Latest of job '" + jobName + "' on ref '" + refName + "'";
		else
			return "Latest of job '" + jobName + "'";
	}

}
