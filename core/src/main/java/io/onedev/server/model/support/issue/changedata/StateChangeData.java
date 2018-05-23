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
import io.onedev.server.web.page.project.issues.issuedetail.activities.activity.StateChangePanel;
import io.onedev.utils.HtmlUtils;

public class StateChangeData extends FieldChangeData {

	private static final long serialVersionUID = 1L;

	private final String state;
	
	private String comment;
	
	public StateChangeData(String state, Map<String, IssueField> oldFields, Map<String, IssueField> newFields, 
			@Nullable String comment) {
		super(oldFields, newFields);
		this.state = state;
		this.comment = comment;
	}

	public String getState() {
		return state;
	}

	@Override
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public Component render(String componentId, IssueChange change) {
		return new StateChangePanel(componentId) {
			
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
			return String.format("[%s] Issue #%d: %s", state, issue.getNumber(), issue.getTitle());  
		else 
			return "changed state to \"" + state + "\"";
	}
	
	@Override
	public String describeAsHtml(IssueChange change) {
		String escapedName = HtmlUtils.escapeHtml(change.getUser().getDisplayName());
		String escapedState = HtmlUtils.escapeHtml(state);
		StringBuilder builder = new StringBuilder(String.format("<b>%s changed state to &quot;%s&quot;</b>", 
				escapedName, escapedState));
		builder.append("<p style='margin: 16px 0;'>");
		builder.append(DiffUtils.diffAsHtml(getLines(getOldFields()), getLines(getNewFields())));
		if (comment != null) {
			builder.append("<p style='margin: 16px 0;'>");
			builder.append(OneDev.getInstance(MarkdownManager.class).escape(comment));			
		}
		return builder.toString();
	}

}
