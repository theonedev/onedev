package io.onedev.server.buildspec.job.projectdependency;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;

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
	
	private RefNameProvider refNameProvider;
	
	@Editable(order=100)
	@ChoiceProvider(value = "getJobChoices", tagsMode = true)
	@NotEmpty
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	@Editable(name="Branch/Tag", order=200, placeholder="Any branch/tag", description="Optionally restrict above job to a branch or tag")
	@Nullable
	public RefNameProvider getRefNameProvider() {
		return refNameProvider;
	}

	public void setRefNameProvider(@Nullable RefNameProvider refNameProvider) {
		this.refNameProvider = refNameProvider;
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
		return OneDev.getInstance(BuildService.class).findLatest(project, jobName,
				refNameProvider != null ? refNameProvider.getRefName() : null);
	}

	@Override
	public String getDescription() {
		if (refNameProvider instanceof BranchProvider branchProvider)
			return "Latest of job '" + jobName + "' on branch '" + branchProvider.getBranchPattern() + "'";
		else if (refNameProvider instanceof TagProvider tagProvider)
			return "Latest of job '" + jobName + "' on tag '" + tagProvider.getTagPattern() + "'";
		else
			return "Latest of job '" + jobName + "'";
	}

}
