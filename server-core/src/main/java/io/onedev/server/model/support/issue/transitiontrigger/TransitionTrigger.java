package io.onedev.server.model.support.issue.transitiontrigger;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import io.onedev.server.annotation.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryUpdater;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.annotation.Editable;

@Editable
public abstract class TransitionTrigger implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String issueQuery;
	
	@Editable(order=1000, name="Applicable Issues", placeholder="All", description=""
			+ "Optionally specify issues applicable for this transition. Leave empty for all issues. ")
	@IssueQuery(withOrder = false)
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
	
	public Collection<String> getUndefinedFields() {
		return new HashSet<>();
	}

	public boolean fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		return true;
	}
	
	public IssueQueryUpdater getQueryUpdater() {
		return new IssueQueryUpdater() {

			@Override
			protected Usage getUsage() {
				return new Usage().add("applicable issues");
			}

			@Override
			protected boolean isAllowEmpty() {
				return true;
			}

			@Override
			protected String getIssueQuery() {
				return issueQuery;
			}

			@Override
			protected void setIssueQuery(String issueQuery) {
				TransitionTrigger.this.issueQuery = issueQuery;
			}
			
		};
	}
	
	public abstract String getDescription();
	
}
