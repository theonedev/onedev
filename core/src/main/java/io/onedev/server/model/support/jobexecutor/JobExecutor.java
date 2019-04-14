package io.onedev.server.model.support.jobexecutor;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.onedev.commons.utils.stringmatch.ChildAwareMatcher;
import io.onedev.commons.utils.stringmatch.Matcher;
import io.onedev.server.OneDev;
import io.onedev.server.cache.CommitInfoManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.RefInfo;
import io.onedev.server.model.Project;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.editable.annotation.ProjectPatterns;

@Editable
public abstract class JobExecutor implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String ENVIRONMENT_WORKSPACE = "onedev-workspace";
	
	private boolean enabled = true;
	
	private String projects;
	
	private String branches;
	
	private String jobs;
	
	private String environments;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	@Editable(order=100, name="Applicable Projects", 
			description="Optionally specify space-separated projects applicable for this executor. Use * or ? for wildcard match. "
					+ "Leave empty to match all projects")
	@ProjectPatterns
	@NameOfEmptyValue("All")
	public String getProjects() {
		return projects;
	}

	public void setProjects(String projects) {
		this.projects = projects;
	}

	@Editable(order=200, name="Applicable Branches", 
			description="Optionally specify space-separated branches applicable for this executor. When run a job "
					+ "against a particular commit, all branches able to reaching the commit will be checked. "
					+ "Use * or ? for wildcard match. Leave empty to match all branches.")
	@Patterns
	@NameOfEmptyValue("All")
	public String getBranches() {
		return branches;
	}

	public void setBranches(String branches) {
		this.branches = branches;
	}

	@Editable(order=300, name="Applicable Jobs", 
			description="Optionally specify space-separated jobs applicable for this executor. Use * or ? for wildcard match. "
					+ "Leave empty to match all jobs")
	@Patterns
	@NameOfEmptyValue("All")
	public String getJobs() {
		return jobs;
	}

	public void setJobs(String jobs) {
		this.jobs = jobs;
	}

	@Editable(order=400, name="Applicable Environments", 
			description="Optionally specify space-separated environments applicable for this executor. Use * or ? for wildcard match. "
					+ "Leave empty to match all images")
	@Patterns
	@NameOfEmptyValue("All")
	public String getEnvironments() {
		return environments;
	}

	public void setEnvironments(String environments) {
		this.environments = environments;
	}
	
	public abstract void execute(String environment, File workspace, Map<String, String> envVars, 
			List<String> commands, @Nullable SourceSnapshot snapshot, PatternSet collectFiles, 
			Logger logger);

	public final boolean isApplicable(Project project, ObjectId commitId, String jobName, String image) {
		Matcher matcher = new ChildAwareMatcher();

		if (isEnabled() 
				&& (getProjects() == null || PatternSet.fromString(getProjects()).matches(matcher, project.getName()))
				&& (getJobs() == null || PatternSet.fromString(getJobs()).matches(matcher, jobName))
				&& (getEnvironments() == null || PatternSet.fromString(getEnvironments()).matches(matcher, image))) {
			if (getBranches() == null)
				return true;
			
			CommitInfoManager commitInfoManager = OneDev.getInstance(CommitInfoManager.class);
			Collection<ObjectId> descendants = commitInfoManager.getDescendants(project, Sets.newHashSet(commitId));
			descendants.add(commitId);
		
			PatternSet branchPatterns = PatternSet.fromString(getBranches());
			for (RefInfo ref: project.getBranches()) {
				String branchName = Preconditions.checkNotNull(GitUtils.ref2branch(ref.getRef().getName()));
				if (descendants.contains(ref.getPeeledObj()) && branchPatterns.matches(matcher, branchName))
					return true;
			}
		}
		return false;
	}
	
	public boolean hasCapacity() {
		return true;
	}
	
}
