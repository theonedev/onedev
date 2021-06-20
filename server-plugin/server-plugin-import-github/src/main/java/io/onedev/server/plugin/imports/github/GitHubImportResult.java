package io.onedev.server.plugin.imports.github;

import java.util.HashSet;
import java.util.Set;

import org.unbescape.html.HtmlEscape;

public class GitHubImportResult {

	Set<String> nonExistentLogins = new HashSet<>();
	
	Set<String> unmappedIssueLabels = new HashSet<>();
	
	Set<String> nonExistentMilestones = new HashSet<>();
	
	boolean issuesImported = false;
	
	public String toHtml(String leadingText) {
		StringBuilder feedback = new StringBuilder(leadingText);
		
		boolean hasNotice = false;
		
		if (!nonExistentMilestones.isEmpty() || !unmappedIssueLabels.isEmpty() 
				|| !nonExistentLogins.isEmpty() || issuesImported) {
			hasNotice = true;
		}
		
		if (hasNotice)
			feedback.append("<br><br><b>NOTE:</b><ul>");
		
		if (!nonExistentMilestones.isEmpty()) {
			feedback.append("<li> Non existent milestones: " 
					+ HtmlEscape.escapeHtml5(nonExistentMilestones.toString()));
		}
		if (!unmappedIssueLabels.isEmpty()) { 
			feedback.append("<li> GitHub issue labels not mapped to OneDev custom field: " 
					+ HtmlEscape.escapeHtml5(unmappedIssueLabels.toString()));
		}
		if (!nonExistentLogins.isEmpty()) {
			feedback.append("<li> GitHub logins without public email or public email can not be mapped to OneDev account: " 
					+ HtmlEscape.escapeHtml5(nonExistentLogins.toString()));
		}
		
		if (issuesImported)
			feedback.append("<li> Attachments in issue description and comments are not imported due to GitHub limitation");
		
		if (hasNotice)
			feedback.append("</ul>");
		
		return feedback.toString();
		
	}
}
