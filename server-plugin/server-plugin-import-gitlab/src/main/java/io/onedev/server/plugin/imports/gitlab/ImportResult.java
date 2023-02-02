package io.onedev.server.plugin.imports.gitlab;

import org.unbescape.html.HtmlEscape;

import java.util.*;

public class ImportResult {

	private static final int MAX_DISPLAY_ENTRIES = 100;
	
	Set<String> nonExistentLogins = new HashSet<>();
	
	Set<String> unmappedIssueLabels = new HashSet<>();
	
	Set<String> nonExistentMilestones = new HashSet<>();
	
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
		
		boolean hasNotice = false;
		
		if (!nonExistentMilestones.isEmpty() || !unmappedIssueLabels.isEmpty() 
				|| !nonExistentLogins.isEmpty() || !tooLargeAttachments.isEmpty()
				|| !errorAttachments.isEmpty()) { 
			hasNotice = true;
		}
		
		if (hasNotice)
			feedback.append("<br><br><b>NOTE:</b><ul>");
		
		if (!nonExistentMilestones.isEmpty()) 
			feedback.append(getEntryFeedback("Non existent milestones", nonExistentMilestones));
		if (!unmappedIssueLabels.isEmpty()) 
			feedback.append(getEntryFeedback("GitLab issue labels not mapped to OneDev custom field", unmappedIssueLabels));
		if (!nonExistentLogins.isEmpty()) {
			feedback.append(getEntryFeedback("GitLab logins without email or email can not be mapped to OneDev account", 
					nonExistentLogins));
		}
		if (!tooLargeAttachments.isEmpty()) 
			feedback.append(getEntryFeedback("Too large attachments", tooLargeAttachments));
		if (!errorAttachments.isEmpty()) 
			feedback.append(getEntryFeedback("Failed to download attachments", errorAttachments));
		
		if (hasNotice)
			feedback.append("</ul>");
		
		return feedback.toString();
		
	}
}
