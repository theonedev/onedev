package io.onedev.server.model.support.issue.changedata;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import io.onedev.server.model.IssueAction;
import io.onedev.server.model.support.CommentSupport;
import io.onedev.server.model.support.issue.IssueField;
import io.onedev.server.web.page.project.issues.issuedetail.activities.activity.ActionDataPanel;

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
	}

	@Override
	protected List<String> getOldLines() {
		List<String> oldLines = super.getOldLines();
		oldLines.add(0, "State: " + oldState);
		return oldLines;
	}

	@Override
	protected List<String> getNewLines() {
		List<String> newLines = super.getOldLines();
		newLines.add(0, "State: " + newState);
		return newLines;
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
	public Component render(String componentId, IssueAction action) {
		return new ActionDataPanel(componentId) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected IssueAction getAction() {
				return action;
			}
		};
	}

	@Override
	public String getDescription() {
		return "changed state";
	}
	
}
