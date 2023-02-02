package io.onedev.server.plugin.imports.youtrack;

import org.unbescape.html.HtmlEscape;

import java.util.*;

public class ImportResult {

	private static final int MAX_DISPLAY_ENTRIES = 100;
	
	Set<String> nonExistentLogins = new LinkedHashSet<>();
	
	Set<String> unmappedIssueTags = new LinkedHashSet<>();
	
	Set<String> unmappedIssueFields = new LinkedHashSet<>();
	
	Set<String> unmappedIssueLinks = new LinkedHashSet<>();
	
	Set<String> unmappedIssueStates = new LinkedHashSet<>();
	
	Map<String, String> mismatchedIssueFields = new LinkedHashMap<>();
	
	Set<String> tooLargeAttachments = new LinkedHashSet<>();
	
	private String getEntryFeedback(String entryDescription, Collection<String> entries) {
		if (entries.size() > MAX_DISPLAY_ENTRIES) {
			List<String> entriesToDisplay = new ArrayList<>(entries).subList(0, MAX_DISPLAY_ENTRIES);
			return "<li> " + entryDescription + ": " + HtmlEscape.escapeHtml5(entriesToDisplay.toString()) + " and more";
		} else {
			return "<li> " + entryDescription + ": " + HtmlEscape.escapeHtml5(entries.toString());
		}
	}
	
	public String toHtml(String leadingText) {
		StringBuilder feedback = new StringBuilder(HtmlEscape.escapeHtml5(leadingText));
		
		boolean hasNotes = 
				!unmappedIssueStates.isEmpty() 
				|| !unmappedIssueFields.isEmpty() 
				|| !mismatchedIssueFields.isEmpty() 
				|| !unmappedIssueTags.isEmpty()
				|| !unmappedIssueLinks.isEmpty()
				|| !nonExistentLogins.isEmpty()
				|| !tooLargeAttachments.isEmpty();

		if (hasNotes)
			feedback.append("<br><br><b>NOTE:</b><ul>");
		
		if (!unmappedIssueStates.isEmpty()) { 
			feedback.append(getEntryFeedback("Unmapped YouTrack issue states (using OneDev initial state)", 
					unmappedIssueStates));
		}
		if (!unmappedIssueFields.isEmpty())  
			feedback.append(getEntryFeedback("Unmapped YouTrack issue fields", unmappedIssueFields));
		if (!unmappedIssueLinks.isEmpty())  
			feedback.append(getEntryFeedback("Unmapped YouTrack issue links", unmappedIssueLinks));
		if (!mismatchedIssueFields.isEmpty()) { 
			feedback.append("<li> YouTrack issue fields mapped to wrong type of OneDev issue field: ");
			feedback.append("<ul>");
			
			int displayedEntries = 0;
			for (Map.Entry<String, String> entry: mismatchedIssueFields.entrySet()) { 
				feedback.append("<li>")
						.append(HtmlEscape.escapeHtml5(entry.getKey()))
						.append(" : ")
						.append(HtmlEscape.escapeHtml5(entry.getValue()));
				if (displayedEntries++ >= MAX_DISPLAY_ENTRIES)
					break;
			}
			if (mismatchedIssueFields.size() > MAX_DISPLAY_ENTRIES)
				feedback.append("<li> And more...");
			feedback.append("</ul>");
		}
		if (!unmappedIssueTags.isEmpty()) { 
			feedback.append(getEntryFeedback("YouTrack issue tags not mapped to OneDev custom field", 
					unmappedIssueTags));
		}
		if (!nonExistentLogins.isEmpty()) {
			feedback.append(getEntryFeedback("YouTrack logins without email or email can not be mapped to OneDev account", 
					nonExistentLogins));
		}
		if (!tooLargeAttachments.isEmpty()) {
			feedback.append(getEntryFeedback("Too large attachments", tooLargeAttachments));
		}
		
		if (hasNotes)
			feedback.append("</ul>");
		
		return feedback.toString();
	}
	
}
