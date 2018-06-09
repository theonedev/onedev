package io.onedev.server.model.support.issue.changedata;

import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import io.onedev.server.OneDev;
import io.onedev.server.manager.MarkdownManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.support.issue.IssueField;
import io.onedev.server.util.diff.DiffUtils;
import io.onedev.server.web.page.project.issues.issuedetail.activities.activity.ChangeDataPanel;
import io.onedev.utils.HtmlUtils;

public class StateChangeData extends FieldChangeData {

	private static final long serialVersionUID = 1L;

	private final String oldState;
	
	private final String newState;
	
	private String comment;
	
	public StateChangeData(String oldState, String newState, Map<String, IssueField> oldFields, 
			Map<String, IssueField> newFields, @Nullable String comment) {
		super(oldFields, newFields);
		this.oldState = oldState;
		this.newState = newState;
		this.comment = comment;
		
		oldLines.add(0, "State: " + oldState);
		newLines.add(0, "State: " + newState);
	}

	public String getNewState() {
		return newState;
	}

	public String getOldState() {
		return oldState;
	}

	@Override
	public CommentSupport getCommentSupport() {
		return new CommentSupport() {

			private static final long serialVersionUID = 1L;

			@Override
			public String getComment() {
				return comment;
			}

			@Override
			public void setComment(String comment) {
				StateChangeData.this.comment = comment;
			}
			
		};
	}
	
	@Override
	public Component render(String componentId, IssueChange change) {
		return new ChangeDataPanel(componentId) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected StateChangeData getChangeData() {
				return StateChangeData.this;
			}
			
			@Override
			protected IssueChange getChange() {
				return change;
			}
		};
	}

	@Override
	public String getTitle(IssueChange change, boolean external) {
		Issue issue = change.getIssue();
		if (external) 
			return String.format("[%s] Issue #%d: %s", newState, issue.getNumber(), issue.getTitle());  
		else 
			return "changed state";
	}
	
	@Override
	public String describeAsHtml(IssueChange change) {
		String escapedName = HtmlUtils.escapeHtml(change.getUser().getDisplayName());
		StringBuilder builder = new StringBuilder(String.format("<b>%s changed state</b>", escapedName));
		builder.append("<p style='margin: 16px 0;'>");
		builder.append(DiffUtils.diffAsHtml(getOldLines(), getNewLines(), true));
		if (comment != null) {
			builder.append("<p style='margin: 16px 0;'>");
			builder.append(OneDev.getInstance(MarkdownManager.class).escape(comment));			
		}
		return builder.toString();
	}

}
