package io.onedev.server.notification;

import io.onedev.server.OneDev;
import io.onedev.server.web.UrlManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.ProjectScopedCommit;
import io.onedev.server.util.PropertyChange;
import io.onedev.server.web.asset.emoji.Emojis;
import io.onedev.server.web.asset.fieldcompare.FieldCompareCssResourceReference;
import io.onedev.server.web.component.codecomment.referencedfrom.ReferencedFromCodeCommentPanel;
import io.onedev.server.web.component.commit.info.CommitInfoPanel;
import io.onedev.server.web.component.issue.referencedfrom.ReferencedFromIssuePanel;
import io.onedev.server.web.component.pullrequest.referencedfrom.ReferencedFromPullRequestPanel;
import io.onedev.server.web.page.project.ProjectPage;
import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.revwalk.RevCommit;
import org.unbescape.html.HtmlEscape;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ActivityDetail implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String htmlVersion;
	
	private final String textVersion;
	
	public ActivityDetail(String htmlVersion, String textVersion) {
		this.htmlVersion = htmlVersion;
		this.textVersion = textVersion;
	}
	
	public String getHtmlVersion() {
		return htmlVersion;
	}

	public String getTextVersion() {
		return textVersion;
	}
	
	public Component render(String componentId) {
		return new Label(componentId, Emojis.getInstance().apply(htmlVersion)) {

			private static final long serialVersionUID = 1L;

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.render(CssHeaderItem.forReference(new FieldCompareCssResourceReference()));
			}
			
		}.setEscapeModelStrings(false);
	}
	
	private static String compareAsHtml(Map<String, String> oldProperties, Map<String, String> newProperties, 
			boolean hideNameIfOnlyOneRow) {
		List<PropertyChange> changes = PropertyChange.listOf(oldProperties, newProperties);

		StringBuilder builder = new StringBuilder();
		
		builder.append("<table class='field-compare' style='border:1px solid #EBEDF3; border-collapse:collapse;'>");
		builder.append("  <thead>");
		builder.append("    <tr>");
		if (changes.size() != 1 || !hideNameIfOnlyOneRow) 
			builder.append("      <th style='padding:0.4em 0.6em; border-bottom:1px solid #EBEDF3; font-size:0.9em; text-align:left;'>Name</th>");
		builder.append("      <th style='padding:0.4em 0.6em; border-bottom:1px solid #EBEDF3; font-size:0.9em; text-align:left;'>Previous Value</th>");
		builder.append("      <th style='padding:0.4em 0.6em; border-bottom:1px solid #EBEDF3; font-size:0.9em; text-align:left;'>Current Value</th>");
		builder.append("    </tr>");
		builder.append("  </thead>");
		builder.append("  <tbody>");
		for (PropertyChange change: changes) {
			builder.append("<tr>");
			if (changes.size() != 1 || !hideNameIfOnlyOneRow) { 
				builder.append("  <td style='padding:0.4em 0.6em; font-size:0.9em; border-bottom:1px solid #EBEDF3; text-align:left;'>");
				builder.append("<pre style='margin:0;'>" + HtmlEscape.escapeHtml5(change.getName()) + "</pre>");
				builder.append("  </td>");
			}
			builder.append("  <td style='padding:0.4em 0.6em; font-size:0.9em; border-bottom:1px solid #EBEDF3; text-align:left;'>");
			if (change.getOldValue() != null)
				builder.append("<pre style='margin:0;'>" + HtmlEscape.escapeHtml5(change.getOldValue()) + "</pre>");
			else
				builder.append("<i>empty</i>");
			builder.append("  </td>");
			builder.append("  <td style='padding:0.4em 0.6em; font-size:0.9em; border-bottom:1px solid #EBEDF3; font-size:0.9em; text-align:left;'>");
			if (change.getNewValue() != null)
				builder.append("<pre style='margin:0;'>" + HtmlEscape.escapeHtml5(change.getNewValue()) + "</pre>");
			else
				builder.append("<i>empty</i>");
			builder.append("  </td>");
			builder.append("</tr>");
		}
		builder.append("  </tbody>");
		builder.append(" </table>");
		
		return builder.toString();
	}

	private static String compareAsText(Map<String, String> oldProperties, Map<String, String> newProperties, 
			boolean hideNameIfOnlyOneRow) {
		List<PropertyChange> changes = PropertyChange.listOf(oldProperties, newProperties);

		StringBuilder builder = new StringBuilder();

		for (PropertyChange change: changes) {
			builder.append("----------------------------------------\n");
			if (changes.size() != 1 || !hideNameIfOnlyOneRow) {
				builder.append("Name: ").append(change.getName());
				builder.append("\n");
			}
			builder.append("Previous Value: ");
			if (change.getOldValue() != null)
				builder.append(change.getOldValue());
			else
				builder.append("<empty>");
			builder.append("\n");
			builder.append("Current Value: ");
			if (change.getNewValue() != null)
				builder.append(change.getNewValue());
			else
				builder.append("<empty>");
			builder.append("\n");
		}
		builder.append("----------------------------------------\n");
		
		return builder.toString();
	}
	
	public static ActivityDetail compare(Map<String, String> oldProperties, Map<String, String> newProperties, 
			boolean hideNameIfOnlyOneRow) {
		String htmlVersion = compareAsHtml(oldProperties, newProperties, hideNameIfOnlyOneRow);
		String textVersion = compareAsText(oldProperties, newProperties, hideNameIfOnlyOneRow);
		return new ActivityDetail(htmlVersion, textVersion);
	}
	
	public static ActivityDetail referencedFrom(CodeComment comment) {
		String url = OneDev.getInstance(UrlManager.class).urlFor(comment);
		String htmlVersion = String.format("<div><a href='%s'>%s</a></div>", 
				url, HtmlEscape.escapeHtml5(comment.getMark().getPath()));
		String textVersion = comment.getMark().getPath() + "\n";  

		Long commentId = comment.getId();
		
		return new ActivityDetail(htmlVersion, textVersion) {

			private static final long serialVersionUID = 1L;

			@Override
			public Component render(String componentId) {
				return new ReferencedFromCodeCommentPanel(componentId, commentId);
			}
			
		};
	}
	
	public static ActivityDetail referencedFrom(Issue issue) {
		String url = OneDev.getInstance(UrlManager.class).urlFor(issue);
		String htmlVersion = String.format("<div><a href='%s'>[%s] %s</a></div>", 
				url, issue.getFQN(), HtmlEscape.escapeHtml5(issue.getTitle()));
		String textVersion = String.format("[%s] %s\n", issue.getFQN(), issue.getTitle());
		
		Long issueId = issue.getId();
		return new ActivityDetail(htmlVersion, textVersion) {

			private static final long serialVersionUID = 1L;

			@Override
			public Component render(String componentId) {
				return new ReferencedFromIssuePanel(componentId, issueId);
			}
			
		};
	}
	
	public static ActivityDetail referencedFrom(PullRequest request) {
		String url = OneDev.getInstance(UrlManager.class).urlFor(request);
		String htmlVersion = String.format("<div><a href='%s'>[%s] %s</a></div>", 
				url, request.getFQN(), HtmlEscape.escapeHtml5(request.getTitle()));
		String textVersion = String.format("[%s] %s\n", request.getFQN(), request.getTitle());
		
		Long requestId = request.getId();
		return new ActivityDetail(htmlVersion, textVersion) {

			private static final long serialVersionUID = 1L;

			@Override
			public Component render(String componentId) {
				return new ReferencedFromPullRequestPanel(componentId, requestId);
			}
			
		};
	}

	public static ActivityDetail referencedFrom(ProjectScopedCommit commit) {
		RevCommit revCommit = commit.getProject().getRevCommit(commit.getCommitId(), true);
		String url = OneDev.getInstance(UrlManager.class).urlFor(commit.getProject(), commit.getCommitId());
		String htmlVersion = String.format("<div><a href='%s'>[%s] %s</a></div>",
				url, commit.getFQN(), HtmlEscape.escapeHtml5(revCommit.getShortMessage()));
		String textVersion = String.format("[%s] %s\n", commit.getFQN(), revCommit.getShortMessage());

		return new ActivityDetail(htmlVersion, textVersion) {

			private static final long serialVersionUID = 1L;

			@Override
			public Component render(String componentId) {
				return new CommitInfoPanel(componentId, Model.of(commit)) {

					@Override
					protected Project getProject() {
						return ((ProjectPage)getPage()).getProject();
					}
				};
			}

		};
	}
	
}
