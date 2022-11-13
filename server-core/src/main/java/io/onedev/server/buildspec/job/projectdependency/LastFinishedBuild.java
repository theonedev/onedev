package io.onedev.server.buildspec.job.projectdependency;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.util.EditContext;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable(order=100, name="Last Finished of Specified Job")
public class LastFinishedBuild implements BuildProvider {

	private static final long serialVersionUID = 1L;
	
	private String jobName;
	
	private String refName;
	
	@Editable(order=100)
	@OmitName
	@ChoiceProvider("getJobChoices")
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
			jobNames.addAll(OneDev.getInstance(BuildManager.class).getAccessibleJobNames(project));
			Collections.sort(jobNames);
		}
		return jobNames;
	}
	
	@Override
	public Build getBuild(Project project) {
		return OneDev.getInstance(BuildManager.class).findLastFinished(project, jobName, refName);
	}

	@Override
	public String getDescription() {
		if (refName != null)
			return "Last finished of job '" + jobName + "' on ref '" + refName + "'";
		else
			return "Last finished of job '" + jobName + "'";
	}

}
