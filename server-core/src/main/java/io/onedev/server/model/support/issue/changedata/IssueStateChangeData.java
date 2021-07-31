package io.onedev.server.model.support.issue.changedata;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.util.CommentAware;
import io.onedev.server.util.Input;

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
	public String getActivity() {
		return "changed state to '" + newState + "'";
	}
	
	@Override
	public ActivityDetail getActivityDetail() {
		return ActivityDetail.compare(getOldFieldValues(), getNewFieldValues(), true);
	}
	
}
