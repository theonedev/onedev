package io.onedev.server.buildspec.job.trigger;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.buildspec.job.SubmitReason;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

public abstract class PullRequestTrigger extends JobTrigger {

	private static final long serialVersionUID = 1L;

	private String branches;
	
	private String paths;
	
	@Editable(name="Target Branches", order=100, description="Optionally specify space-separated "
			+ "target branches of the pull requests to check. Use '**', '*' or '?' for <a href='$docRoot/pages/path-wildcard.md' target='_blank'>path wildcard match</a>. "
			+ "Prefix with '-' to exclude. Leave empty to match all branches")
	@Patterns(suggester = "suggestBranches", path=true)
	@NameOfEmptyValue("Any branch")
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
	
	@Editable(name="Touched Files", order=200, 
			description="Optionally specify space-separated files to check. Use '**', '*' or '?' for <a href='$docRoot/pages/path-wildcard.md' target='_blank'>path wildcard match</a>. "
					+ "Prefix with '-' to exclude. Leave empty to match all files")
	@Patterns(suggester = "getPathSuggestions", path=true)
	@NameOfEmptyValue("Any file")
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
			Collection<String> changedFiles = GitUtils.getChangedFiles(request.getTargetProject().getRepository(), 
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
	protected SubmitReason triggerMatches(PullRequest request) {
		String targetBranch = request.getTargetBranch();
		Matcher matcher = new PathMatcher();
		if ((branches == null || PatternSet.parse(branches).matches(matcher, targetBranch)) 
				&& touchedFile(request)) {
			return new SubmitReason() {

				@Override
				public String getRefName() {
					return request.getMergeRef();
				}

				@Override
				public PullRequest getPullRequest() {
					return request;
				}

				@Override
				public String getDescription() {
					return "Pull request #" + request.getNumber() + " is opened/updated";
				}
				
			};
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
