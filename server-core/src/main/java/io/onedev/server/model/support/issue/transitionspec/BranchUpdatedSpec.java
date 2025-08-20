package io.onedev.server.model.support.issue.transitionspec;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.IssueQuery;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.issue.IssueQueryLexer;
import io.onedev.server.web.util.SuggestionUtils;

@Editable(order=500, name="Code is committed")
public class BranchUpdatedSpec extends AutoSpec {

	private static final long serialVersionUID = 1L;

	private String branches;

	private String commitMessages;	

	public BranchUpdatedSpec() {
		setIssueQuery(io.onedev.server.search.entity.issue.IssueQuery
				.getRuleName(IssueQueryLexer.FixedInCurrentCommit));		
	}
	
	@Editable(order=200, name="Applicable Branches", placeholder="Any branch", description="Optionally specify space-separated branches "
			+ "applicable for this transition. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. "
			+ "Prefix with '-' to exclude. Leave empty to match all")
	@Patterns(suggester = "suggestBranches", path=true)
	public String getBranches() {
		return branches;
	}

	public void setBranches(String branches) {
		this.branches = branches;
	}
	
	@Editable(order = 300, name = "Applicable Commit Messages", placeholder = "Any commit message", description = "Optionally specify space-separated commit messages "
			+ "applicable for this transition. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all")
	@Patterns
	public String getCommitMessages() {
		return commitMessages;
	}

	public void setCommitMessages(String commitMessages) {
		this.commitMessages = commitMessages;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestBranches(String matchWith) {
		Project project = Project.get();
		if (project != null)
			return SuggestionUtils.suggestBranches(project, matchWith);
		else
			return new ArrayList<>();
	}

	@Editable(order=9900, name="Applicable Issues", placeholder="All", description="Optionally specify issues applicable "
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
	public String getTriggerDescription() {
		if (branches != null) {
			if (commitMessages != null) {
				return MessageFormat.format(_T("code is committed to branches \"{0}\" with message \"{1}\""), branches, commitMessages);
			} else {
				return MessageFormat.format(_T("code is committed to branches \"{0}\""), branches);
			}
		} else {
			if (commitMessages != null) {
				return MessageFormat.format(_T("code is committed with message \"{0}\""), commitMessages);
			} else {
				return _T("code is committed");
			}
		}
	}
	
}
