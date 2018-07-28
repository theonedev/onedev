package io.onedev.server.model.support.issue.changedata;

import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.util.lang.Objects;

import io.onedev.server.model.IssueAction;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.support.CommentSupport;
import io.onedev.server.model.support.issue.IssueField;
import io.onedev.server.web.page.project.issues.issuedetail.activities.activity.ActionDataPanel;

public class BatchUpdateData extends FieldChangeData {

	private static final long serialVersionUID = 1L;

	private final String oldState;
	
	private final String newState;
	
	private final String oldMilestone;
	
	private final String newMilestone;
	
	private String comment;
	
	public BatchUpdateData(String oldState, String newState, @Nullable Milestone oldMilestone, @Nullable Milestone newMilestone, 
			Map<String, IssueField> oldFields, Map<String, IssueField> newFields, @Nullable String comment) {
		super(oldFields, newFields);
		this.oldState = oldState;
		this.newState = newState;
		this.oldMilestone = oldMilestone!=null?oldMilestone.getName():null;
		this.newMilestone = newMilestone!=null?newMilestone.getName():null;
		this.comment = comment;
		
		if (!Objects.equal(oldMilestone, newMilestone)) {
			if (oldMilestone != null)
				oldLines.add(0, "Milestone: " + oldMilestone.getName());
			else
				oldLines.add(0, "Milestone: ");
			if (newMilestone != null)
				newLines.add(0, "Milestone: " + newMilestone.getName());
			else
				newLines.add(0, "Milestone: ");
		}

		if (!Objects.equal(oldState, newState)) {
			oldLines.add(0, "State: " + oldState);
			newLines.add(0, "State: " + newState);
		}
	}

	public String getNewState() {
		return newState;
	}

	public String getOldState() {
		return oldState;
	}

	public String getOldMilestone() {
		return oldMilestone;
	}

	public String getNewMilestone() {
		return newMilestone;
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
				BatchUpdateData.this.comment = comment;
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
		return "batch edited";
	}
	
}
