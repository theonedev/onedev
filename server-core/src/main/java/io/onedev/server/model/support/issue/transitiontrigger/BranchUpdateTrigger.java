package io.onedev.server.model.support.issue.transitiontrigger;

import java.util.ArrayList;
import java.util.List;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.issue.IssueQueryLexer;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.IssueQuery;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

@Editable(order=500, name="Code is committed")
public class BranchUpdateTrigger extends TransitionTrigger {

	private static final long serialVersionUID = 1L;

	private String branches;

	public BranchUpdateTrigger() {
		setIssueQuery(io.onedev.server.search.entity.issue.IssueQuery
				.getRuleName(IssueQueryLexer.FixedInCurrentCommit));		
	}
	
	@Editable(order=200, name="Applicable Branches", placeholder="Any branch", description="Optionally specify space-separated branches "
			+ "applicable for this trigger. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. "
			+ "Prefix with '-' to exclude. Leave empty to match all")
	@Patterns(suggester = "suggestBranches", path=true)
	public String getBranches() {
		return branches;
	}

	public void setBranches(String branches) {
		this.branches = branches;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestBranches(String matchWith) {
		Project project = Project.get();
		if (project != null)
			return SuggestionUtils.suggestBranches(project, matchWith);
		else
			return new ArrayList<>();
	}

	@Editable(order=1000, name="Applicable Issues", placeholder="All", description="Optionally specify issues applicable "
			+ "for this transition. Leave empty for all issues")
	@IssueQuery(withOrder = false, withCurrentCommitCriteria = true)
	@Override
	public String getIssueQuery() {
		return super.getIssueQuery();
	}

	public void setIssueQuery(String issueQuery) {
		super.setIssueQuery(issueQuery);
	}
	
	@Override
	public String getDescription() {
		if (branches != null)
			return "code is committed to branches '" + branches + "'";
		else
			return "code is committed to any branch";
	}
	
}
