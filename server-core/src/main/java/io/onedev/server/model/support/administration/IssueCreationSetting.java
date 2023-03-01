package io.onedev.server.model.support.administration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.field.supply.FieldSupply;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.FieldNamesProvider;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
public class IssueCreationSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	private String senderEmails;
	
	private String applicableProjects;
	
	private boolean confidential;
	
	private List<FieldSupply> issueFields = new ArrayList<>();

	@Editable(order=100, name="Applicable Senders", placeholder="Any sender", 
			description="Specify space-separated sender email addresses applicable for this entry. "
					+ "Use '*' or '?' for wildcard match. Prefix with '-' to exclude. "
					+ "Leave empty to match all senders")
	@Patterns
	public String getSenderEmails() {
		return senderEmails;
	}

	public void setSenderEmails(String senderEmails) {
		this.senderEmails = senderEmails;
	}

	@Editable(order=150, placeholder="Any project", description="Specify space-separated projects applicable for this entry. "
			+ "Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to "
			+ "match all projects")
	@Patterns(suggester="suggestProjects")
	public String getApplicableProjects() {
		return applicableProjects;
	}

	public void setApplicableProjects(String applicableProjects) {
		this.applicableProjects = applicableProjects;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestProjects(String matchWith) {
		return SuggestionUtils.suggestProjectPaths(matchWith);
	}
	
	@Editable(order=200, description="Whether or not created issue should be confidential")
	public boolean isConfidential() {
		return confidential;
	}

	public void setConfidential(boolean confidential) {
		this.confidential = confidential;
	}

	@Editable(order=300)
	@FieldNamesProvider("getFieldNames")
	@OmitName
	@Valid
	public List<FieldSupply> getIssueFields() {
		return issueFields;
	}

	public void setIssueFields(List<FieldSupply> issueFields) {
		this.issueFields = issueFields;
	}
	
	@SuppressWarnings("unused")
	private static Collection<String> getFieldNames() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting().getFieldNames();
	}
	
	public boolean isProjectAuthorized(Project project) {
		return applicableProjects == null 
				|| PatternSet.parse(applicableProjects).matches(new PathMatcher(), project.getPath());
	}
	
	public Set<String> getUndefinedFields() {
		Set<String> undefinedFields = new HashSet<>();
		for (FieldSupply supply: getIssueFields()) 
			undefinedFields.addAll(supply.getUndefinedFields());
		return undefinedFields;
	}

	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		Collection<UndefinedFieldValue> undefinedFieldValues = new HashSet<>(); 
		for (FieldSupply supply: getIssueFields()) 
			undefinedFieldValues.addAll(supply.getUndefinedFieldValues());
		return undefinedFieldValues;
	}
	
	public void fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		for (Iterator<FieldSupply> it = getIssueFields().iterator(); it.hasNext();) {
			if (!it.next().fixUndefinedFields(resolutions))
				it.remove();
		}
	}

	public void fixUndefinedFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions) {
		for (Iterator<FieldSupply> it = getIssueFields().iterator(); it.hasNext();) {
			if (!it.next().fixUndefinedFieldValues(resolutions))
				it.remove();
		}
	}
	
}