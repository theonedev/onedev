package io.onedev.server.model.support.issue.changedata;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.Group;
import io.onedev.server.model.Iteration;
import io.onedev.server.model.User;
import io.onedev.server.notification.ActivityDetail;

public class IssueIterationChangeData extends IssueChangeData {

	private static final long serialVersionUID = 1L;

	private final List<String> oldIterations;
	
	private final List<String> newIterations;
	
	public IssueIterationChangeData(List<Iteration> oldIterations, List<Iteration> newIterations) {
		this.oldIterations = oldIterations.stream().map(it->it.getName()).collect(Collectors.toList());
		this.newIterations = newIterations.stream().map(it->it.getName()).collect(Collectors.toList());
	}
	
	public List<String> getOldIterations() {
		return oldIterations;
	}

	public List<String> getNewIterations() {
		return newIterations;
	}

	@Override
	public String getActivity() {
		return "changed iterations";
	}

	@Override
	public Map<String, Collection<User>> getNewUsers() {
		return new HashMap<>();
	}

	@Override
	public Map<String, Group> getNewGroups() {
		return new HashMap<>();
	}

	@Override
	public boolean affectsListing() {
		return true;
	}

	@Override
	public ActivityDetail getActivityDetail() {
		Map<String, String> oldFieldValues = new HashMap<>();
		oldFieldValues.put("Iterations", StringUtils.join(oldIterations));
		Map<String, String> newFieldValues = new HashMap<>();
		newFieldValues.put("Iterations", StringUtils.join(newIterations));
		return ActivityDetail.compare(oldFieldValues, newFieldValues, true);
	}
	
}
