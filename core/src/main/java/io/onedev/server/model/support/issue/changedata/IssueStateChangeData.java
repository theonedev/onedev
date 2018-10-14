package io.onedev.server.model.support.issue.changedata;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import io.onedev.server.model.IssueChange;
import io.onedev.server.util.CommentSupport;
import io.onedev.server.util.IssueField;
import io.onedev.server.util.diff.DiffSupport;
import io.onedev.server.web.component.issue.activities.activity.DiffAndCommentAwarePanel;

public class IssueStateChangeData extends IssueFieldChangeData {

	private static final long serialVersionUID = 1L;

	private final String oldState;
	
	private final String newState;
	
	private String comment;
	
	public IssueStateChangeData(String oldState, String newState, Map<String, IssueField> oldFields, 
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
		List<String> newLines = super.getNewLines();
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
				IssueStateChangeData.this.comment = comment;
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
						return IssueStateChangeData.this.getOldLines();
					}

					@Override
					public List<String> getNewLines() {
						return IssueStateChangeData.this.getNewLines();
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
		return "changed state";
	}
	
}
