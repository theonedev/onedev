package io.onedev.server.model.support.issue.changedata;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.util.CommentAware;
import io.onedev.server.util.Input;
import io.onedev.server.web.component.issue.activities.activity.IssueFieldChangePanel;

public class IssueStateChangeData extends IssueFieldChangeData {

	private static final long serialVersionUID = 1L;

	private final String oldState;
	
	private final String newState;
	
	private String comment;
	
	public IssueStateChangeData(String oldState, String newState, 
			Map<String, Input> oldFields, Map<String, Input> newFields, 
			@Nullable String comment) {
		super(oldFields, newFields);
		this.oldState = oldState;
		this.newState = newState;
		this.comment = comment;
	}

	@Override
	public Map<String, String> getOldFieldValues() {
		Map<String, String> oldFieldValues = new LinkedHashMap<>();
		oldFieldValues.put("State", oldState);
		oldFieldValues.putAll(super.getOldFieldValues());
		return oldFieldValues;
	}

	@Override
	public Map<String, String> getNewFieldValues() {
		Map<String, String> newFieldValues = new LinkedHashMap<>();
		newFieldValues.put("State", newState);
		newFieldValues.putAll(super.getNewFieldValues());
		return newFieldValues;
	}

	public String getNewState() {
		return newState;
	}

	public String getOldState() {
		return oldState;
	}

	@Override
	public CommentAware getCommentAware() {
		return new CommentAware() {

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
		Long changeId = change.getId();
		
		return new IssueFieldChangePanel(componentId, true) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected IssueChange getChange() {
				return OneDev.getInstance(IssueChangeManager.class).load(changeId);
			}

		};
	}

	@Override
	public String getActivity(Issue withIssue) {
		String activity = "changed state to '" + newState + "'";
		if (withIssue != null)
			activity += " for issue " + withIssue.getNumberAndTitle();
		return activity;
	}
	
}
