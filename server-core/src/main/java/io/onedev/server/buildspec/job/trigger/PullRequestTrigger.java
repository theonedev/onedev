package io.onedev.server.buildspec.job.trigger;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.buildspec.job.TriggerMatch;
import io.onedev.server.service.ProjectService;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.commons.utils.match.Matcher;
import io.onedev.commons.utils.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.util.SuggestionUtils;
import org.eclipse.jgit.lib.Repository;

import org.jspecify.annotations.Nullable;
import java.util.Collection;
import java.util.List;

public abstract class PullRequestTrigger extends JobTrigger {

	private static final long serialVersionUID = 1L;

	private String branches;
	
	private String paths;
	
	@Editable(name="Target Branches", placeholder="Any branch", order=100, description=""
			+ "Optionally specify space-separated target branches of the pull requests to check. "
			+ "Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. "
			+ "Prefix with '-' to exclude. Leave empty to match all branches")
	@Patterns(suggester = "suggestBranches", path=true)
	public String getBranches() {
		return branches;
	}

	public void setBranches(String branches) {
		this.branches = branches;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestBranches(String matchWith) {
		return SuggestionUtils.suggestBranches(Project.get(), matchWith);
	}
	
	@Editable(name="Touched Files", order=200, placeholder="Any file", description=""
			+ "Optionally specify space-separated files to check. "
			+ "Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. "
			+ "Prefix with '-' to exclude. Leave empty to match all files")
	@Patterns(suggester = "getPathSuggestions", path=true)
	public String getPaths() {
		return paths;
	}

	public void setPaths(String paths) {
		this.paths = paths;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> getPathSuggestions(String matchWith) {
		return SuggestionUtils.suggestBlobs(Project.get(), matchWith);
	}

	private boolean touchedFile(PullRequest request) {
		if (getPaths() != null) {
			Repository repository = OneDev.getInstance(ProjectService.class)
					.getRepository(request.getTargetProject().getId());
			Collection<String> changedFiles = GitUtils.getChangedFiles(repository, 
					request.getBaseCommit(), request.getLatestUpdate().getHeadCommit());
			PatternSet patternSet = PatternSet.parse(getPaths());
			Matcher matcher = new PathMatcher();
			for (String changedFile: changedFiles) {
				if (patternSet.matches(matcher, changedFile))
					return true;
			}
			return false;
		} else {
			return true;
		}
	}
	
	@Nullable
	protected TriggerMatch triggerMatches(PullRequest request, String submitReason) {
		String targetBranch = request.getTargetBranch();
		Matcher matcher = new PathMatcher();
		if ((branches == null || PatternSet.parse(branches).matches(matcher, targetBranch)) 
				&& touchedFile(request)) {
			
			return new TriggerMatch(request.getMergeRef(), request, null, getParamMatrix(), 
					getExcludeParamMaps(), submitReason);
		}
		return null;
	}
	
	protected String getTriggerDescription(String action) {
		String description;
		if (getBranches() != null && getPaths() != null)
			description = String.format("When " + action + " pull requests targeting branches '%s' and touching files '%s'", getBranches(), getPaths());
		else if (getBranches() != null)
			description = String.format("When " + action + " pull requests targeting branches '%s'", getBranches());
		else if (getPaths() != null)
			description = String.format("When " + action + " pull requests touching files '%s'", getPaths());
		else
			description = "When " + action + " pull requests";
		return description;
	}

}
