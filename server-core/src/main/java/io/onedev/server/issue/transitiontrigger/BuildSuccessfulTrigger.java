package io.onedev.server.issue.transitiontrigger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.model.Project;
import io.onedev.server.util.Usage;
import io.onedev.server.util.ValueSetEdit;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
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
	
	private String issueQuery = "fixed in current build";
	
	@Editable(order=100, name="Applicable Jobs", description="Optionally specify space-separated jobs "
			+ "applicable for this trigger. Use * or ? for wildcard match")
	@Patterns(suggester = "suggestJobs")
	@NameOfEmptyValue("Any job")
	public String getJobNames() {
		return jobNames;
	}

	public void setJobNames(String jobNames) {
		this.jobNames = jobNames;
	}

	@Editable(order=200, name="Applicable Branches", description="Optionally specify space-separated branches "
			+ "applicable for this trigger. Use * or ? for wildcard match")
	@Patterns(suggester = "suggestBranches")
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
	
	@Editable(order=300, description="Specify an issue query to filter issues eligible for this transition."
			+ "This query will be combined with 'from states' criteria of this transition")
	@IssueQuery(withCurrentUserCriteria = false, withCurrentBuildCriteria = true)
	@NotEmpty
	public String getIssueQuery() {
		return issueQuery;
	}

	public void setIssueQuery(String issueQuery) {
		this.issueQuery = issueQuery;
	}

	@Override
	public Usage onDeleteBranch(String branchName) {
		Usage usage = new Usage();
		PatternSet patternSet = PatternSet.fromString(getBranches());
		if (patternSet.getIncludes().contains(branchName) || patternSet.getExcludes().contains(branchName))
			usage.add("applicable branches");
		return usage.prefix("build successful trigger");
	}

	@Override
	public void onRenameState(String oldName, String newName) {
		try {
			io.onedev.server.search.entity.issue.IssueQuery query = 
					io.onedev.server.search.entity.issue.IssueQuery.parse(null, issueQuery, false, true, true);
			query.onRenameState(oldName, newName);
			issueQuery = query.toString();
		} catch (Exception e) {
		}
	}

	@Override
	public boolean onDeleteState(String stateName) {
		try {
			io.onedev.server.search.entity.issue.IssueQuery query = 
					io.onedev.server.search.entity.issue.IssueQuery.parse(null, issueQuery, false, true, true);
			if (query.onDeleteState(stateName)) {
				return true;
			} else {
				issueQuery = query.toString();
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void onRenameField(String oldName, String newName) {
		try {
			io.onedev.server.search.entity.issue.IssueQuery query = 
					io.onedev.server.search.entity.issue.IssueQuery.parse(null, issueQuery, false, true, true);
			query.onRenameField(oldName, newName);
			issueQuery = query.toString();
		} catch (Exception e) {
		}
	}

	@Override
	public boolean onDeleteField(String fieldName) {
		try {
			io.onedev.server.search.entity.issue.IssueQuery query = 
					io.onedev.server.search.entity.issue.IssueQuery.parse(null, issueQuery, false, true, true);
			if (query.onDeleteField(fieldName)) {
				return true;
			} else {
				issueQuery = query.toString();
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean onEditFieldValues(String fieldName, ValueSetEdit valueSetEdit) {
		try {
			io.onedev.server.search.entity.issue.IssueQuery query = 
					io.onedev.server.search.entity.issue.IssueQuery.parse(null, issueQuery, false, true, true);
			if (query.onEditFieldValues(fieldName, valueSetEdit)) {
				return true;
			} else {
				issueQuery = query.toString();
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public Collection<String> getUndefinedStates() {
		try {
			return io.onedev.server.search.entity.issue.IssueQuery
					.parse(null, issueQuery, false, true, true).getUndefinedStates();
		} catch (Exception e) {
			return new HashSet<>();
		}
	}

	@Override
	public Collection<String> getUndefinedFields() {
		try {
			return io.onedev.server.search.entity.issue.IssueQuery
					.parse(null, issueQuery, false, true, true).getUndefinedFields();
		} catch (Exception e) {
			return new HashSet<>();
		}
	}

	@Override
	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		try {
			return io.onedev.server.search.entity.issue.IssueQuery
					.parse(null, issueQuery, false, true, true).getUndefinedFieldValues();
		} catch (Exception e) {
			return new HashSet<>();
		}
	}
	
}
