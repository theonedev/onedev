package io.onedev.server.issue.transitiontrigger;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import io.onedev.server.util.Usage;
import io.onedev.server.util.ValueSetEdit;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.IssueQuery;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable
public abstract class TransitionTrigger implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String issueQuery;
	
	@Editable(order=1000, name="Applicable Issues", description="Optionally specify issues applicable for this transition. Leave empty for all issues")
	@IssueQuery(withOrder = false, withCurrentUserCriteria = false, withCurrentBuildCriteria = true, 
			withCurrentPullRequestCriteria = true, withCurrentCommitCriteria = true)
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
	
	public void onRenameState(String oldName, String newName) {
		try {
			io.onedev.server.search.entity.issue.IssueQuery parsedQuery = 
					io.onedev.server.search.entity.issue.IssueQuery.parse(null, issueQuery, false, true, true, true, true);
			parsedQuery.onRenameState(oldName, newName);
			issueQuery = parsedQuery.toString();
		} catch (Exception e) {
		}
	}

	public boolean onDeleteState(String stateName) {
		try {
			io.onedev.server.search.entity.issue.IssueQuery parsedQuery = 
					io.onedev.server.search.entity.issue.IssueQuery.parse(null, issueQuery, false, true, true, true, true);
			if (parsedQuery.onDeleteState(stateName)) {
				return true;
			} else {
				issueQuery = parsedQuery.toString();
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	public void onRenameField(String oldName, String newName) {
		try {
			io.onedev.server.search.entity.issue.IssueQuery parsedQuery = 
					io.onedev.server.search.entity.issue.IssueQuery.parse(null, issueQuery, false, true, true, true, true);
			parsedQuery.onRenameField(oldName, newName);
			issueQuery = parsedQuery.toString();
		} catch (Exception e) {
		}
	}

	public boolean onDeleteField(String fieldName) {
		try {
			io.onedev.server.search.entity.issue.IssueQuery parsedQuery = 
					io.onedev.server.search.entity.issue.IssueQuery.parse(null, issueQuery, false, true, true, true, true);
			if (parsedQuery.onDeleteField(fieldName)) {
				return true;
			} else {
				issueQuery = parsedQuery.toString();
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	public boolean onEditFieldValues(String fieldName, ValueSetEdit valueSetEdit) {
		try {
			io.onedev.server.search.entity.issue.IssueQuery parsedQuery = 
					io.onedev.server.search.entity.issue.IssueQuery.parse(null, issueQuery, false, true, true, true, true);
			if (parsedQuery.onEditFieldValues(fieldName, valueSetEdit)) {
				return true;
			} else {
				issueQuery = parsedQuery.toString();
				return false;
			}
		} catch (Exception e) {
			return false;
		}
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
	
	public abstract String getDescription();
}
