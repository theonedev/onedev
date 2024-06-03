package io.onedev.server.model.support.issue.changedata;

import io.onedev.server.model.Group;
import io.onedev.server.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class IssueCommentRemoveData extends IssueChangeData {

	private static final long serialVersionUID = 1L;
	
	@Override
	public String getActivity() {
		return "removed comment";
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
		return false;
	}

	@Override
	public boolean isMinor() {
		return true;
	}

}
