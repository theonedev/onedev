package io.onedev.server.search.entity.issue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;

public abstract class IssueQueryUpdater {

	public void onRenameLink(String oldName, String newName) {
		setIssueQuery(IssueQuery
				.parse(null, getIssueQuery(), new IssueQueryParseOption().enableAll(true), false)
				.onRenameLink(oldName, newName)
				.toString());
	}

	public Usage onDeleteLink(String linkName) {
		if (IssueQuery
				.parse(null, getIssueQuery(), new IssueQueryParseOption().enableAll(true), false)
				.isUsingLink(linkName)) {
			return getUsage();
		} else {
			return new Usage();
		}
	}
	
	public void onMoveProject(String oldPath, String newPath) {
		setIssueQuery(IssueQuery
				.parse(null, getIssueQuery(), new IssueQueryParseOption().enableAll(true), false)
				.onMoveProject(oldPath, newPath)
				.toString());
	}

	public Usage onDeleteProject(String projectPath) {
		if (IssueQuery
				.parse(null, getIssueQuery(), new IssueQueryParseOption().enableAll(true), false)
				.isUsingProject(projectPath)) {
			return getUsage();
		} else {
			return new Usage();
		}
	}
	
	public Collection<String> getUndefinedStates() {
		try {
			return IssueQuery
					.parse(null, getIssueQuery(), new IssueQueryParseOption().enableAll(true), false)
					.getUndefinedStates();
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}
	
	public Collection<String> getUndefinedFields() {
		try {
			return IssueQuery
					.parse(null, getIssueQuery(), new IssueQueryParseOption().enableAll(true), false)
					.getUndefinedFields();
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		try {
			return IssueQuery
					.parse(null, getIssueQuery(), new IssueQueryParseOption().enableAll(true), false)
					.getUndefinedFieldValues();
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}
	
	public boolean fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions) {
		try {
			IssueQuery parsedQuery = IssueQuery.parse(null, getIssueQuery(), new IssueQueryParseOption().enableAll(true), false);
			if (parsedQuery.fixUndefinedStates(resolutions))
				setIssueQuery(parsedQuery.toString());
			else if (isAllowEmpty())
				setIssueQuery(null);
			else
				return false;
		} catch (Exception e) {
		}
		return true;
	}

	public boolean fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		try {
			IssueQuery parsedQuery = IssueQuery.parse(null, getIssueQuery(), new IssueQueryParseOption().enableAll(true), false);
			if (parsedQuery.fixUndefinedFields(resolutions))
				setIssueQuery(parsedQuery.toString());
			else if (isAllowEmpty())
				setIssueQuery(null);
			else
				return false;
		} catch (Exception e) {
		}
		return true;
	}
	
	public boolean fixUndefinedFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions) {
		try {
			IssueQuery parsedQuery = IssueQuery.parse(null, getIssueQuery(), new IssueQueryParseOption().enableAll(true), false);
			if (parsedQuery.fixUndefinedFieldValues(resolutions))
				setIssueQuery(parsedQuery.toString());
			else if (isAllowEmpty())
				setIssueQuery(null);
			else
				return false;
		} catch (Exception e) {
		}
		return true;
	}
	
	protected abstract Usage getUsage();
	
	protected abstract boolean isAllowEmpty();
	
	@Nullable
	protected abstract String getIssueQuery();
	
	protected abstract void setIssueQuery(@Nullable String issueQuery);
	
}
