package io.onedev.server.plugin.imports.youtrack;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.unbescape.html.HtmlEscape;

public class YouTrackImportResult {

	Set<String> nonExistentLogins = new LinkedHashSet<>();
	
	Set<String> unmappedIssueTags = new LinkedHashSet<>();
	
	Set<String> unmappedIssueFields = new LinkedHashSet<>();
	
	Set<String> unmappedIssueStates = new LinkedHashSet<>();
	
	Map<String, String> mismatchedIssueFields = new LinkedHashMap<>();
	
	Set<String> tooLargeAttachments = new LinkedHashSet<>();
	
	public String toHtml(String leadingText) {
		StringBuilder feedback = new StringBuilder(HtmlEscape.escapeHtml5(leadingText));
		
		boolean hasNotes = 
				!unmappedIssueStates.isEmpty() 
				|| !unmappedIssueFields.isEmpty() 
				|| !mismatchedIssueFields.isEmpty() 
				|| !unmappedIssueTags.isEmpty()
				|| !nonExistentLogins.isEmpty()
				|| !tooLargeAttachments.isEmpty();

		if (hasNotes)
			feedback.append("<br><br><b>NOTE:</b><ul>");
		
		if (!unmappedIssueStates.isEmpty()) { 
			feedback.append("<li> Unmapped YouTrack issue states (using OneDev initial state): " 
					+ HtmlEscape.escapeHtml5(unmappedIssueStates.toString()));
		}
		if (!unmappedIssueFields.isEmpty()) { 
			feedback.append("<li> Unmapped YouTrack issue fields: " 
					+ HtmlEscape.escapeHtml5(unmappedIssueFields.toString()));
		}
		if (!mismatchedIssueFields.isEmpty()) { 
			feedback.append("<li> YouTrack issue fields mapped to wrong type of OneDev issue field: ");
			feedback.append("<ul>");
			for (Map.Entry<String, String> entry: mismatchedIssueFields.entrySet()) { 
				feedback.append("<li>")
						.append(HtmlEscape.escapeHtml5(entry.getKey()))
						.append(" : ")
						.append(HtmlEscape.escapeHtml5(entry.getValue()));
			}
			feedback.append("</ul>");
		}
		if (!unmappedIssueTags.isEmpty()) { 
			feedback.append("<li> YouTrack issue tags not mapped to OneDev custom field: " 
					+ HtmlEscape.escapeHtml5(unmappedIssueTags.toString()));
		}
		if (!nonExistentLogins.isEmpty()) {
			feedback.append("<li> YouTrack logins without email or email can not be mapped to OneDev account: " 
					+ HtmlEscape.escapeHtml5(nonExistentLogins.toString()));
		}
		if (!tooLargeAttachments.isEmpty()) {
			feedback.append("<li> Too large attachments: " 
					+ HtmlEscape.escapeHtml5(tooLargeAttachments.toString()));
		}
		
		if (hasNotes)
			feedback.append("</ul>");
		
		return feedback.toString();
	}
	
}
