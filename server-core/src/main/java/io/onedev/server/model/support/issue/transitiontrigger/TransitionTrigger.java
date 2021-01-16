package io.onedev.server.model.support.issue.transitiontrigger;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.IssueQuery;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable
public abstract class TransitionTrigger implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String issueQuery;
	
	@Editable(order=1000, name="Applicable Issues", description="Optionally specify issues applicable for this transition. Leave empty for all issues")
	@IssueQuery(withOrder = false, withCurrentUserCriteria = false, withCurrentBuildCriteria = false, 
			withCurrentPullRequestCriteria = false, withCurrentCommitCriteria = false)
	@NameOfEmptyValue("All")
	public String getIssueQuery() {
		return issueQuery;
	}

	public void setIssueQuery(String issueQuery) {
		this.issueQuery = issueQuery;
	}

	public Usage onDeleteBranch(String branchName) {
		return new Usage();
	}
	
	public void onRenameRole(String oldName, String newName) {
	}

	public Usage onDeleteRole(String roleName) {
		return new Usage();
	}
	
	public Collection<String> getUndefinedStates() {
		try {
			return io.onedev.server.search.entity.issue.IssueQuery
					.parse(null, issueQuery, false, true, true, true, true).getUndefinedStates();
		} catch (Exception e) {
			return new HashSet<>();
		}
	}

	public Collection<String> getUndefinedFields() {
		try {
			return io.onedev.server.search.entity.issue.IssueQuery
					.parse(null, issueQuery, false, true, true, true, true).getUndefinedFields();
		} catch (Exception e) {
			e.printStackTrace();
			return new HashSet<>();
		}
	}

	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		try {
			return io.onedev.server.search.entity.issue.IssueQuery
					.parse(null, issueQuery, false, true, true, true, true).getUndefinedFieldValues();
		} catch (Exception e) {
			return new HashSet<>();
		}
	}	
	
	public boolean fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions) {
		try {
			io.onedev.server.search.entity.issue.IssueQuery parsedQuery = 
					io.onedev.server.search.entity.issue.IssueQuery.parse(null, issueQuery, false, true, true, true, true);
			if (parsedQuery.fixUndefinedStates(resolutions))
				issueQuery = parsedQuery.toString();
			else
				return false;
		} catch (Exception e) {
		}
		return true;
	}
	
	public boolean fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		try {
			io.onedev.server.search.entity.issue.IssueQuery parsedQuery = 
					io.onedev.server.search.entity.issue.IssueQuery.parse(null, issueQuery, false, true, true, true, true);
			if (parsedQuery.fixUndefinedFields(resolutions))
				issueQuery = parsedQuery.toString();
			else
				return false;
		} catch (Exception e) {
		}
		return true;
	}
	
	public boolean fixUndefinedFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions) {
		try {
			io.onedev.server.search.entity.issue.IssueQuery parsedQuery = 
					io.onedev.server.search.entity.issue.IssueQuery.parse(null, issueQuery, false, true, true, true, true);
			if (parsedQuery.fixUndefinedFieldValues(resolutions))
				issueQuery = parsedQuery.toString();
			else 
				return false;
		} catch (Exception e) {
		}
		return true;
	}
	
	public abstract String getDescription();
}
