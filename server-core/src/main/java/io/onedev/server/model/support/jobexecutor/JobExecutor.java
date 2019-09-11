package io.onedev.server.model.support.jobexecutor;

import java.io.Serializable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.launcher.loader.ExtensionPoint;
import io.onedev.commons.utils.stringmatch.ChildAwareMatcher;
import io.onedev.commons.utils.stringmatch.Matcher;
import io.onedev.server.ci.job.JobContext;
import io.onedev.server.model.Project;
import io.onedev.server.util.Usage;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.annotation.BranchPatterns;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.editable.annotation.ProjectPatterns;

@ExtensionPoint
@Editable
public abstract class JobExecutor implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean enabled = true;
	
	private String projects;
	
	private String branches;
	
	private String jobNames;
	
	private String jobEnvironments;

	private int cacheTTL = 7;
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Editable(order=10000, name="Applicable Projects", group="Job Applicability",
			description="Optionally specify space-separated projects applicable for this executor. "
					+ "Use * or ? for wildcard match. Leave empty to match all")
	@ProjectPatterns
	@NameOfEmptyValue("All")
	public String getProjects() {
		return projects;
	}

	public void setProjects(String projects) {
		this.projects = projects;
	}

	@Editable(order=10100, name="Applicable Branches", group="Job Applicability",
			description="Optionally specify space-separated branches applicable for this executor. "
					+ "Use * or ? for wildcard match. Leave empty to match all")
	@BranchPatterns
	@NameOfEmptyValue("All")
	public String getBranches() {
		return branches;
	}

	public void setBranches(String branches) {
		this.branches = branches;
	}

	@Editable(order=10200, name="Applicable Job Names", group="Job Applicability",
			description="Optionally specify space-separated jobs applicable for this executor. "
					+ "Use * or ? for wildcard match. Leave empty to match all")
	@Patterns
	@NameOfEmptyValue("All")
	public String getJobNames() {
		return jobNames;
	}

	public void setJobNames(String jobNames) {
		this.jobNames = jobNames;
	}

	@Editable(order=10300, name="Applicable Job Environments", group="Job Applicability",
			description="Optionally specify space-separated job environments applicable for this executor. "
					+ "Use * or ? for wildcard match. Leave empty to match all")
	@Patterns
	@NameOfEmptyValue("All")
	public String getJobEnvironments() {
		return jobEnvironments;
	}

	public void setJobEnvironments(String jobEnvironments) {
		this.jobEnvironments = jobEnvironments;
	}
	
	@Editable(order=50000, group="More Settings", description="Specify job cache TTL (time to live) by days. "
			+ "OneDev may create multiple job caches even for same cache key to avoid cache conflicts when "
			+ "running jobs concurrently. This setting tells OneDev to remove caches inactive for specified "
			+ "time period to save disk space")
	public int getCacheTTL() {
		return cacheTTL;
	}

	public void setCacheTTL(int cacheTTL) {
		this.cacheTTL = cacheTTL;
	}
	
	public abstract void execute(String jobToken, JobContext context);

	public final boolean isApplicable(Project project, ObjectId commitId, String jobName, String jobEnvironment) {
		Matcher matcher = new ChildAwareMatcher();

		return isEnabled() 
				&& (getProjects() == null || PatternSet.fromString(getProjects()).matches(matcher, project.getName()))
				&& (getJobNames() == null || PatternSet.fromString(getJobNames()).matches(matcher, jobName))
				&& (getJobEnvironments() == null || PatternSet.fromString(getJobEnvironments()).matches(matcher, jobEnvironment))
				&& (getBranches() == null || project.isCommitOnBranches(commitId, getBranches()));
	}
	
	public Usage onDeleteProject(String projectName, int executorIndex) {
		Usage usage = new Usage();
		if (getProjects() != null) {
			PatternSet patternSet = PatternSet.fromString(getProjects());
			if (patternSet.getIncludes().contains(projectName) || patternSet.getExcludes().contains(projectName))
				usage.add("applicable projects");
		} 
		return usage.prefix("job executor #" + executorIndex);
	}
	
	public void onRenameProject(String oldName, String newName) {
		PatternSet patternSet = PatternSet.fromString(getProjects());
		if (patternSet.getIncludes().remove(oldName))
			patternSet.getIncludes().add(newName);
		if (patternSet.getExcludes().remove(oldName))
			patternSet.getExcludes().add(newName);
		setProjects(patternSet.toString());
		if (getProjects().length() == 0)
			setProjects(null);
	}

}
