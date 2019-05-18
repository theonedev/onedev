package io.onedev.server.model.support.jobexecutor;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;

import io.onedev.commons.utils.stringmatch.ChildAwareMatcher;
import io.onedev.commons.utils.stringmatch.Matcher;
import io.onedev.server.ci.job.cache.JobCache;
import io.onedev.server.model.Project;
import io.onedev.server.util.Usage;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.validation.annotation.ExecutorName;
import io.onedev.server.web.editable.annotation.BranchPatterns;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.editable.annotation.ProjectPatterns;
import io.onedev.server.web.editable.annotation.TagPatterns;

@Editable
public abstract class JobExecutor implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String WORKSPACE = "onedev-workspace";
	
	private boolean enabled = true;
	
	private String name;
	
	private String projects;
	
	private String branches;
	
	private String tags;
	
	private String jobs;
	
	private String environments;

	private int cacheTTL = 7;
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	@Editable(order=50, description="Specify a name to identify the executor")
	@ExecutorName
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=10000, name="Restricted Projects", group="Job Restrictions",
			description="Optionally specify projects applicable for this executor. Use space to "
					+ "separate multiple projects, and use * or ? for wildcard match")
	@ProjectPatterns
	@NameOfEmptyValue("No restrictions")
	public String getProjects() {
		return projects;
	}

	public void setProjects(String projects) {
		this.projects = projects;
	}

	@Editable(order=10100, name="Restricted Branches", group="Job Restrictions",
			description="Optionally specify branches the job should be running on in order to use this executor. "
					+ "Use space to separate multiple projects, and use * or ? for wildcard match")
	@BranchPatterns
	@NameOfEmptyValue("No restrictions")
	public String getBranches() {
		return branches;
	}

	public void setBranches(String branches) {
		this.branches = branches;
	}

	@Editable(order=10110, name="Restricted Tags", group="Job Restrictions",
			description="Optionally specify tags the job should be running on in order to use this executor. "
					+ "Use space to separate multiple projects, and use * or ? for wildcard match")
	@TagPatterns
	@NameOfEmptyValue("No restrictions")
	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	@Editable(order=10200, name="Restricted Jobs", group="Job Restrictions",
			description="Optionally specify jobs applicable for this executor. Use space to separate "
					+ "multiple jobs, and use * or ? for wildcard match")
	@Patterns
	@NameOfEmptyValue("No restrictions")
	public String getJobs() {
		return jobs;
	}

	public void setJobs(String jobs) {
		this.jobs = jobs;
	}

	@Editable(order=10300, name="Restricted Environments", group="Job Restrictions",
			description="Optionally specify job environments applicable for this executor. Use space to "
					+ "separate multiple environments, and use * or ? for wildcard match")
	@Patterns
	@NameOfEmptyValue("No restrictions")
	public String getEnvironments() {
		return environments;
	}

	public void setEnvironments(String environments) {
		this.environments = environments;
	}
	
	@Editable(order=50000, group="More Settings", description="Specify job cache TTL (time to live) by days. OneDev may create "
			+ "multiple job caches even for same cache key to avoid cache conflicts when running jobs "
			+ "concurrently. This setting tells OneDev to remove caches inactive for specified "
			+ "time period to save disk space")
	public int getCacheTTL() {
		return cacheTTL;
	}

	public void setCacheTTL(int cacheTTL) {
		this.cacheTTL = cacheTTL;
	}

	public abstract void execute(String environment, File workspace, Map<String, String> envVars, 
			List<String> commands, @Nullable SourceSnapshot snapshot, Collection<JobCache> caches, 
			PatternSet collectFiles, Logger logger);

	public final boolean isApplicable(Project project, ObjectId commitId, String jobName, String environment) {
		Matcher matcher = new ChildAwareMatcher();

		return isEnabled() 
				&& (getProjects() == null || PatternSet.fromString(getProjects()).matches(matcher, project.getName()))
				&& (getJobs() == null || PatternSet.fromString(getJobs()).matches(matcher, jobName))
				&& (getEnvironments() == null || PatternSet.fromString(getEnvironments()).matches(matcher, environment))
				&& (getBranches() == null || project.isCommitOnBranches(commitId, getBranches())
				&& (getTags() == null || project.isCommitOnTags(commitId, getTags())));
	}
	
	public Usage getProjectUsage(String projectName) {
		Usage usage = new Usage();
		if (getProjects() != null) {
			PatternSet patternSet = PatternSet.fromString(getProjects());
			if (patternSet.getIncludes().contains(projectName) || patternSet.getExcludes().contains(projectName))
				usage.add("restricted projects");
		} 
		return usage.prefix(getName()).prefix("job executor '" + getName() + "'");
	}
	
	public void onProjectRenamed(String oldName, String newName) {
		PatternSet patternSet = PatternSet.fromString(getProjects());
		if (patternSet.getIncludes().remove(oldName))
			patternSet.getIncludes().add(newName);
		if (patternSet.getExcludes().remove(oldName))
			patternSet.getExcludes().add(newName);
		setProjects(patternSet.toString());
	}
	
	public boolean hasCapacity() {
		return true;
	}
	
	public abstract void checkCaches();
	
	public abstract void cleanDir(File dir);
	
}
