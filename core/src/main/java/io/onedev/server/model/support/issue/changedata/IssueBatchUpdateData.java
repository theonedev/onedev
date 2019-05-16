package io.onedev.server.model.support.issue.changedata;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import com.google.common.base.Objects;

import io.onedev.server.model.IssueChange;
import io.onedev.server.model.Milestone;
import io.onedev.server.util.CommentSupport;
import io.onedev.server.util.Input;
import io.onedev.server.util.diff.DiffSupport;
import io.onedev.server.web.component.issue.activities.activity.DiffAndCommentAwarePanel;

public class IssueBatchUpdateData extends IssueFieldChangeData {

	private static final long serialVersionUID = 1L;

	private final String oldState;
	
	private final String newState;
	
	private final String oldMilestone;
	
	private final String newMilestone;
	
	private String comment;
	
	public IssueBatchUpdateData(String oldState, String newState, @Nullable Milestone oldMilestone, @Nullable Milestone newMilestone, 
			Map<String, Input> oldFields, Map<String, Input> newFields, @Nullable String comment) {
		super(oldFields, newFields);
		this.oldState = oldState;
		this.newState = newState;
		this.oldMilestone = oldMilestone!=null?oldMilestone.getName():null;
		this.newMilestone = newMilestone!=null?newMilestone.getName():null;
		this.comment = comment;
	}

	@Override
	protected List<String> getOldLines() {
		List<String> oldLines = super.getOldLines();
		if (!Objects.equal(oldMilestone, newMilestone)) {
			if (oldMilestone != null)
				oldLines.add(0, "Milestone: " + oldMilestone);
			else
				oldLines.add(0, "Milestone: ");
		}
		if (!Objects.equal(oldState, newState)) {
			oldLines.add(0, "State: " + oldState);
		}
		return oldLines;
	}

	@Override
	protected List<String> getNewLines() {
		List<String> newLines = super.getNewLines();
		if (!Objects.equal(oldMilestone, newMilestone)) {
			if (newMilestone != null)
				newLines.add(0, "Milestone: " + newMilestone);
			else
				newLines.add(0, "Milestone: ");
		}
		if (!Objects.equal(oldState, newState)) {
			newLines.add(0, "State: " + newState);
		}
		return newLines;
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
				IssueBatchUpdateData.this.comment = comment;
			}
			
		};
	}

	@Override
	public Component render(String componentId, IssueChange change) {
		return new DiffAndCommentAwarePanel(componentId) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected IssueChange getChange() {
				return change;
			}

			@Override
			protected DiffSupport getDiffSupport() {
				return new DiffSupport() {

					private static final long serialVersionUID = 1L;

					@Override
					public List<String> getOldLines() {
						return IssueBatchUpdateData.this.getOldLines();
					}

					@Override
					public List<String> getNewLines() {
						return IssueBatchUpdateData.this.getNewLines();
					}

					@Override
					public String getOldFileName() {
						return "a.txt";
					}

					@Override
					public String getNewFileName() {
						return "b.txt";
					}
					
				};
			}
		};
	}

	@Override
	public String getDescription() {
		return "batch edited";
	}
	
}
