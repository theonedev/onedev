package io.onedev.server.model.support.issue.transitiontrigger;

import java.util.ArrayList;
import java.util.List;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.issue.IssueQueryLexer;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.IssueQuery;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

public abstract class PullRequestTrigger extends TransitionTrigger {

	private static final long serialVersionUID = 1L;
	
	private String branches;
	
	public PullRequestTrigger() {
		setIssueQuery(io.onedev.server.search.entity.issue.IssueQuery
				.getRuleName(IssueQueryLexer.FixedInCurrentPullRequest));		
	}
	
	@Editable(name="Target Branches", order=100, 
			description="Optionally specify space-separated target branches of the pull requests to check. "
					+ "Use '**', '*' or '?' for <a href='$docRoot/pages/path-wildcard.md' target='_blank'>path wildcard match</a>. "
					+ "Prefix with '-' to exclude. Leave empty to match all branches")
	@Patterns(suggester = "suggestBranches", path=true)
	@NameOfEmptyValue("Any branch")
	public String getBranches() {
		return branches;
	}

	public void setBranches(String branches) {
		this.branches = branches;
	}

	@Editable(order=1000, name="Applicable Issues", description="Optionally specify issues applicable for this transition. Leave empty for all issues")
	@IssueQuery(withOrder = false, withCurrentUserCriteria = false, withCurrentBuildCriteria = false, 
			withCurrentPullRequestCriteria = true, withCurrentCommitCriteria = false)
	@NameOfEmptyValue("All")
	@Override
	public String getIssueQuery() {
		return super.getIssueQuery();
	}

	public void setIssueQuery(String issueQuery) {
		super.setIssueQuery(issueQuery);
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestBranches(String matchWith) {
		if (Project.get() != null)
			return SuggestionUtils.suggestBranches(Project.get(), matchWith);
		else
			return new ArrayList<>();
	}
	
	@Override
	public Usage onDeleteBranch(String branchName) {
		Usage usage = super.onDeleteBranch(branchName);
		PatternSet patternSet = PatternSet.parse(branches);
		if (patternSet.getIncludes().contains(branchName) || patternSet.getExcludes().contains(branchName))
			usage.add("target branches");
		return usage;
	}
	
}
