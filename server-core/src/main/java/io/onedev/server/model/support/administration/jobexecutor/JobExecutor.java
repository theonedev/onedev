package io.onedev.server.model.support.administration.jobexecutor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.lib.ObjectId;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.launcher.loader.ExtensionPoint;
import io.onedev.commons.utils.match.Matcher;
import io.onedev.commons.utils.match.PathMatcher;
import io.onedev.server.OneDev;
import io.onedev.server.ci.job.JobContext;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Project;
import io.onedev.server.util.Usage;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

@ExtensionPoint
@Editable
public abstract class JobExecutor implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean enabled = true;
	
	private String applicableProjects;
	
	private String applicableBranches;
	
	private String applicableJobNames;
	
	private String applicableJobImages;

	private int cacheTTL = 7;
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Editable(order=10000, group="Job Applicability",
			description="Optionally specify space-separated projects applicable for this executor. "
					+ "Use * or ? for wildcard match. Leave empty to match all")
	@Patterns(suggester = "suggestProjects")
	@NameOfEmptyValue("All")
	public String getApplicableProjects() {
		return applicableProjects;
	}

	public void setApplicableProjects(String applicableProjects) {
		this.applicableProjects = applicableProjects;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestProjects(String matchWith) {
		return SuggestionUtils.suggestProjects(matchWith);
	}

	@Editable(order=10100, group="Job Applicability",
			description="Optionally specify space-separated branches applicable for this executor. "
					+ "Use * or ? for wildcard match. Leave empty to match all")
	@Patterns
	@NameOfEmptyValue("All")
	public String getApplicableBranches() {
		return applicableBranches;
	}

	public void setApplicableBranches(String applicableBranches) {
		this.applicableBranches = applicableBranches;
	}

	@Editable(order=10200, group="Job Applicability",
			description="Optionally specify space-separated jobs applicable for this executor. "
					+ "Use * or ? for wildcard match. Leave empty to match all")
	@Patterns
	@NameOfEmptyValue("All")
	public String getApplicableJobNames() {
		return applicableJobNames;
	}

	public void setApplicableJobNames(String applicableJobNames) {
		this.applicableJobNames = applicableJobNames;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestJobNames(String matchWith) {
		List<String> jobNames = new ArrayList<>(OneDev.getInstance(BuildManager.class).getJobNames());
		Collections.sort(jobNames);
		return SuggestionUtils.suggest(jobNames, matchWith);
	}

	@Editable(order=10300, group="Job Applicability",
			description="Optionally specify space-separated job images applicable for this executor. "
					+ "Use * or ? for wildcard match. Leave empty to match all")
	@Patterns
	@NameOfEmptyValue("All")
	public String getApplicableJobImages() {
		return applicableJobImages;
	}

	public void setApplicableJobImages(String applicableJobImages) {
		this.applicableJobImages = applicableJobImages;
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

	public final boolean isApplicable(Project project, ObjectId commitId, String jobName, String jobImage) {
		Matcher matcher = new PathMatcher();

		return isEnabled() 
				&& (getApplicableProjects() == null || PatternSet.fromString(getApplicableProjects()).matches(matcher, project.getName()))
				&& (getApplicableJobNames() == null || PatternSet.fromString(getApplicableJobNames()).matches(matcher, jobName))
				&& (getApplicableJobImages() == null || PatternSet.fromString(getApplicableJobImages()).matches(matcher, jobImage))
				&& (getApplicableBranches() == null || project.isCommitOnBranches(commitId, getApplicableBranches()));
	}
	
	public Usage onDeleteProject(String projectName, int executorIndex) {
		Usage usage = new Usage();
		if (getApplicableProjects() != null) {
			PatternSet patternSet = PatternSet.fromString(getApplicableProjects());
			if (patternSet.getIncludes().contains(projectName) || patternSet.getExcludes().contains(projectName))
				usage.add("applicable projects");
		} 
		return usage.prefix("job executor #" + executorIndex);
	}
	
	public void onRenameProject(String oldName, String newName) {
		PatternSet patternSet = PatternSet.fromString(getApplicableProjects());
		if (patternSet.getIncludes().remove(oldName))
			patternSet.getIncludes().add(newName);
		if (patternSet.getExcludes().remove(oldName))
			patternSet.getExcludes().add(newName);
		setApplicableProjects(patternSet.toString());
		if (getApplicableProjects().length() == 0)
			setApplicableProjects(null);
	}

}
