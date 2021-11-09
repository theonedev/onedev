package io.onedev.server.model.support.issue.changedata;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.Milestone;
import io.onedev.server.util.CommentAware;
import io.onedev.server.util.Input;

public class IssueBatchUpdateData extends IssueFieldChangeData {

	private static final long serialVersionUID = 1L;

	private final String oldState;
	
	private final String newState;
	
	private final List<String> oldMilestones;
	
	private final List<String> newMilestones;
	
	private String comment;
	
	public IssueBatchUpdateData(String oldState, String newState, 
			List<Milestone> oldMilestones, List<Milestone> newMilestones, 
			Map<String, Input> oldFields, Map<String, Input> newFields, 
			@Nullable String comment) {
		super(oldFields, newFields);
		this.oldState = oldState;
		this.newState = newState;
		this.oldMilestones = oldMilestones.stream().map(it->it.getName()).collect(Collectors.toList());
		this.newMilestones = newMilestones.stream().map(it->it.getName()).collect(Collectors.toList());
		this.comment = comment;
	}

	@Override
	public Map<String, String> getOldFieldValues() {
		Map<String, String> oldFieldValues = new LinkedHashMap<>();
		oldFieldValues.put("State", oldState);
		if (!oldMilestones.isEmpty())
			oldFieldValues.put("Milestones", StringUtils.join(oldMilestones));
		oldFieldValues.putAll(super.getOldFieldValues());
		return oldFieldValues;
	}

	@Override
	public Map<String, String> getNewFieldValues() {
		Map<String, String> newFieldValues = new LinkedHashMap<>();
		newFieldValues.put("State", newState);
		if (!newMilestones.isEmpty())
			newFieldValues.put("Milestones", StringUtils.join(newMilestones));
		newFieldValues.putAll(super.getNewFieldValues());
		return newFieldValues;
	}

	public String getNewState() {
		return newState;
	}

	public String getOldState() {
		return oldState;
	}

	public List<String> getOldMilestones() {
		return oldMilestones;
	}

	public List<String> getNewMilestones() {
		return newMilestones;
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
				IssueBatchUpdateData.this.comment = comment;
			}

		};
	}

	@Override
	public String getActivity() {
		return "batch edited";
	}
	
}
