package io.onedev.server.plugin.imports.github;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.onedev.server.model.Issue;
import org.unbescape.html.HtmlEscape;

public class ImportResult {

	private static final int MAX_DISPLAY_ENTRIES = 100;
	
	Set<String> nonExistentLogins = new HashSet<>();
	
	Set<String> unmappedIssueLabels = new HashSet<>();
	
	Set<String> nonExistentMilestones = new HashSet<>();
	
	boolean issuesImported;
	
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
				|| !nonExistentLogins.isEmpty() || issuesImported) {
			hasNotice = true;
		}
		
		if (hasNotice)
			feedback.append("<br><br><b>NOTE:</b><ul>");
		
		if (!nonExistentMilestones.isEmpty()) 
			feedback.append(getEntryFeedback("Non existent milestones", nonExistentMilestones));
		if (!unmappedIssueLabels.isEmpty()) { 
			feedback.append(getEntryFeedback("GitHub issue labels not mapped to OneDev custom field",  
					unmappedIssueLabels));
		}
		if (!nonExistentLogins.isEmpty()) {
			feedback.append(getEntryFeedback("GitHub logins without public email or public email can not be mapped to OneDev account", 
					nonExistentLogins));
		}
		
		if (issuesImported)
			feedback.append("<li> Attachments in issue description and comments are not imported due to GitHub limitation");
		
		if (hasNotice)
			feedback.append("</ul>");
		
		return feedback.toString();
		
	}
}
