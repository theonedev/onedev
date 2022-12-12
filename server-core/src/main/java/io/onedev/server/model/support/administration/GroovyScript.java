package io.onedev.server.model.support.administration;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.Size;

import javax.validation.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.model.Project;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.script.identity.JobIdentity;
import io.onedev.server.util.script.identity.ScriptIdentity;
import io.onedev.server.util.script.identity.SiteAdministrator;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.util.validation.annotation.Code;
import io.onedev.server.util.validation.annotation.RegEx;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.editable.annotation.ShowCondition;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
public class GroovyScript implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String BUILTIN_PREFIX = "builtin:";

	private String name;
	
	private List<String> content;
	
	private boolean canBeUsedByBuildJobs = true;
	
	private String allowedProjects;
	
	private String allowedBranches;

	@Editable(order=100)
	@RegEx(pattern="^(?!" + BUILTIN_PREFIX + ").*$", message="Name is not allowed to start with '" + BUILTIN_PREFIX + "'")
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=300)
	@Code(language = Code.GROOVY)
	@Size(min=1, message="May not be empty")
	public List<String> getContent() {
		return content;
	}

	public void setContent(List<String> content) {
		this.content = content;
	}
	
	@Editable(order=350)
	public boolean isCanBeUsedByBuildJobs() {
		return canBeUsedByBuildJobs;
	}

	public void setCanBeUsedByBuildJobs(boolean canBeUsedByBuildJobs) {
		this.canBeUsedByBuildJobs = canBeUsedByBuildJobs;
	}
	
	@SuppressWarnings("unused")
	private static boolean isCanBeUsedByBuildJobsEnabled() {
		return (boolean) EditContext.get().getInputValue("canBeUsedByBuildJobs");
	}

	@Editable(order=400, placeholder="All", description="Optionally specify space-separated projects allowed to "
			+ "execute this script. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude. "
			+ "Leave empty to allow all")
	@Patterns(suggester="suggestProjects", path=true)
	@ShowCondition("isCanBeUsedByBuildJobsEnabled")
	public String getAllowedProjects() {
		return allowedProjects;
	}
	
	public void setAllowedProjects(String allowedProjects) {
		this.allowedProjects = allowedProjects;
	}
	
	@Editable(order=500, placeholder="All", description="Optionally specify space-separated branches allowed to "
		+ "execute this script. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. "
		+ "Prefix with '-' to exclude. Leave empty to allow all")
	@Patterns(path=true)
	@ShowCondition("isCanBeUsedByBuildJobsEnabled")
	public String getAllowedBranches() {
		return allowedBranches;
	}
	
	public void setAllowedBranches(String allowedBranches) {
		this.allowedBranches = allowedBranches;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestProjects(String matchWith) {
		return SuggestionUtils.suggestProjectPaths(matchWith);
	}
		
	public final boolean isAuthorized(ScriptIdentity identity) {
		if (identity instanceof SiteAdministrator) { 
			return true;
		} else if (isCanBeUsedByBuildJobs() && identity instanceof JobIdentity) {
			JobIdentity jobIdentity = (JobIdentity) identity;
			return (getAllowedProjects() == null || PatternSet.parse(getAllowedProjects()).matches(new PathMatcher(), jobIdentity.getProject().getPath()))
					&& (getAllowedBranches() == null || jobIdentity.getProject().isCommitOnBranches(jobIdentity.getCommitId(), getAllowedBranches()));
		} else {
			return false;
		}
	}
	
	public Usage onDeleteProject(String projectPath) {
		Usage usage = new Usage();
		if (Project.containsPath(getAllowedProjects(), projectPath))
			usage.add("allowed projects");
		return usage;
	}
	
	public void onMoveProject(String oldPath, String newPath) {
		setAllowedProjects(Project.substitutePath(getAllowedProjects(), oldPath, newPath));
	}

}
