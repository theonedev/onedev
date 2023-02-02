package io.onedev.server.plugin.imports.jiracloud;

import org.unbescape.html.HtmlEscape;

import java.util.*;

public class ImportResult {

	private static final int MAX_DISPLAY_ENTRIES = 100;
	
	Set<String> nonExistentLogins = new HashSet<>();
	
	Set<String> unmappedIssueStatuses = new HashSet<>();
	
	Set<String> unmappedIssueTypes = new HashSet<>();
	
	Set<String> unmappedIssuePriorities = new HashSet<>();
	
	Set<String> tooLargeAttachments = new LinkedHashSet<>();
	
	Set<String> errorAttachments = new LinkedHashSet<>();
	
	private String getEntryFeedback(String entryDescription, Collection<String> entries) {
		if (entries.size() > MAX_DISPLAY_ENTRIES) {
			List<String> entriesToDisplay = new ArrayList<>(entries).subList(0, MAX_DISPLAY_ENTRIES);
			return "<li> " + entryDescription + ": " + HtmlEscape.escapeHtml5(entriesToDisplay.toString()) + " and more";
		} else {
			return "<li> " + entryDescription + ": " + HtmlEscape.escapeHtml5(entries.toString());
		}
	}
	
	public String toHtml(String leadingText) {
		StringBuilder feedback = new StringBuilder(leadingText);
		
		feedback.append("<br><br><b>NOTE:</b><ul>");

		feedback.append("<li> JIRA issue custom fields are not imported");
		if (!unmappedIssueStatuses.isEmpty()) 
			feedback.append(getEntryFeedback("JIRA issue statuses not mapped to OneDev custom field", unmappedIssueStatuses));
		if (!unmappedIssueTypes.isEmpty()) 
			feedback.append(getEntryFeedback("JIRA issue types not mapped to OneDev custom field", unmappedIssueTypes));
		if (!unmappedIssuePriorities.isEmpty()) 
			feedback.append(getEntryFeedback("JIRA issue priorities not mapped to OneDev custom field", unmappedIssuePriorities));
		if (!nonExistentLogins.isEmpty()) {
			feedback.append(getEntryFeedback("JIRA accounts not available in OneDev (matching by full name)", nonExistentLogins));
		}
		if (!tooLargeAttachments.isEmpty()) 
			feedback.append(getEntryFeedback("Too large attachments", tooLargeAttachments));
		if (!errorAttachments.isEmpty()) 
			feedback.append(getEntryFeedback("Failed to download attachments", errorAttachments));
		
		feedback.append("</ul>");
		
		return feedback.toString();
		
	}
	
}
