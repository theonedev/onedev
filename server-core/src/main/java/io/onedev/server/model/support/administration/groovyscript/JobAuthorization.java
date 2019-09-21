package io.onedev.server.model.support.administration.groovyscript;

import java.util.List;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.match.Matcher;
import io.onedev.commons.utils.match.PathMatcher;
import io.onedev.server.util.Usage;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.scriptidentity.JobIdentity;
import io.onedev.server.util.scriptidentity.ScriptIdentity;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
public class JobAuthorization implements ScriptAuthorization {

	private static final long serialVersionUID = 1L;
	
	private String allowedProjects;
	
	private String allowedBranches;

	@Editable(order=400, description="Optionally specify space-separated projects allowed to "
			+ "execute this script. Use * or ? for wildcard match. Leave empty to allow all")
	@Patterns(suggester = "suggestProjects")
	@NameOfEmptyValue("All")
	public String getAllowedProjects() {
		return allowedProjects;
	}
	
	public void setAllowedProjects(String allowedProjects) {
		this.allowedProjects = allowedProjects;
	}
	
	@Editable(order=500, description="Optionally specify space-separated branches allowed to "
		+ "execute this script. Use * or ? for wildcard match. Leave empty to allow all")
	@Patterns
	@NameOfEmptyValue("All")
	public String getAllowedBranches() {
		return allowedBranches;
	}
	
	public void setAllowedBranches(String allowedBranches) {
		this.allowedBranches = allowedBranches;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestProjects(String matchWith) {
		return SuggestionUtils.suggestProjects(matchWith);
	}
	
	@Override
	public boolean isAuthorized(ScriptIdentity identity) {
		if (identity instanceof JobIdentity) {
			JobIdentity jobIdentity = (JobIdentity) identity;
			Matcher matcher = new PathMatcher();
			return (getAllowedProjects() == null || PatternSet.fromString(getAllowedProjects()).matches(matcher, jobIdentity.getProject().getName()))
					&& (getAllowedBranches() == null || jobIdentity.getProject().isCommitOnBranches(jobIdentity.getCommitId(), getAllowedBranches()));
		} else {
			return false;
		}
	}

	public Usage onDeleteProject(String projectName) {
		Usage usage = new Usage();
		if (getAllowedProjects() != null) {
			PatternSet patternSet = PatternSet.fromString(getAllowedProjects());
			if (patternSet.getIncludes().contains(projectName) || patternSet.getExcludes().contains(projectName))
				usage.add("allowed projects");
		} 
		return usage.prefix("job authorization");
	}
	
	public void onRenameProject(String oldName, String newName) {
		PatternSet patternSet = PatternSet.fromString(getAllowedProjects());
		if (patternSet.getIncludes().remove(oldName))
			patternSet.getIncludes().add(newName);
		if (patternSet.getExcludes().remove(oldName))
			patternSet.getExcludes().add(newName);
		setAllowedProjects(patternSet.toString());
		if (getAllowedProjects().length() == 0)
			setAllowedProjects(null);
	}
	
}
