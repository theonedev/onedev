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

@Editable(order=400, name="Build is successful")
public class BuildSuccessfulTrigger extends TransitionTrigger {

	private static final long serialVersionUID = 1L;
	
	private String jobNames;
	
	private String branches;
	
	public BuildSuccessfulTrigger() {
		setIssueQuery(io.onedev.server.search.entity.issue.IssueQuery
				.getRuleName(IssueQueryLexer.FixedInCurrentBuild));		
	}
	
	@Editable(order=100, name="Applicable Jobs", description="Optionally specify space-separated jobs "
			+ "applicable for this trigger. Use '*' or '?' for wildcard match. Prefix with '-' to exclude. "
			+ "Leave empty to match all")
	@Patterns(suggester = "suggestJobs")
	@NameOfEmptyValue("Any job")
	public String getJobNames() {
		return jobNames;
	}

	public void setJobNames(String jobNames) {
		this.jobNames = jobNames;
	}

	@Editable(order=200, name="Applicable Branches", description="Optionally specify space-separated branches "
			+ "applicable for this trigger. Use '**', '*' or '?' for <a href='$docRoot/pages/path-wildcard.md' target='_blank'>path wildcard match</a>. "
			+ "Prefix with '-' to exclude. Leave empty to match all")
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
		Project project = Project.get();
		if (project != null)
			return SuggestionUtils.suggestBranches(project, matchWith);
		else
			return new ArrayList<>();
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestJobs(String matchWith) {
		Project project = Project.get();
		if (project != null)
			return SuggestionUtils.suggest(project.getJobNames(), matchWith);
		else
			return new ArrayList<>();
	}
	
	@Editable(order=1000, name="Applicable Issues", description="Optionally specify issues applicable for this transition. Leave empty for all issues")
	@IssueQuery(withOrder = false, withCurrentUserCriteria = false, withCurrentBuildCriteria = true, 
			withCurrentPullRequestCriteria = false, withCurrentCommitCriteria = false)
	@NameOfEmptyValue("All")
	@Override
	public String getIssueQuery() {
		return super.getIssueQuery();
	}

	public void setIssueQuery(String issueQuery) {
		super.setIssueQuery(issueQuery);
	}

	@Override
	public Usage onDeleteBranch(String branchName) {
		Usage usage = super.onDeleteBranch(branchName);
		PatternSet patternSet = PatternSet.parse(getBranches());
		if (patternSet.getIncludes().contains(branchName) || patternSet.getExcludes().contains(branchName))
			usage.add("applicable branches");
		return usage;
	}
	
	@Override
	public String getDescription() {
		if (jobNames != null) {
			if (branches != null)
				return "Build is successful for jobs '" + jobNames + "' on branches '" + branches + "'";
			else
				return "Build is successful for jobs '" + jobNames + "' on any branch";
		} else {
			if (branches != null)
				return "Build is successful for any job on branches '" + branches + "'";
			else
				return "Build is successful for any job and branch";
		}
	}
	
}
