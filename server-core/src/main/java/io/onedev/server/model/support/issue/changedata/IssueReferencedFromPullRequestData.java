package io.onedev.server.model.support.issue.changedata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entityreference.ReferencedFromAware;
import io.onedev.server.model.Group;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.notification.ActivityDetail;

public class IssueReferencedFromPullRequestData extends IssueChangeData implements ReferencedFromAware<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final Long requestId;
	
	public IssueReferencedFromPullRequestData(PullRequest request) {
		this.requestId = request.getId();
	}
	
	public Long getRequestId() {
		return requestId;
	}

	@Override
	public String getActivity() {
		return "referenced from pull request";
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
	public PullRequest getReferencedFrom() {
		return OneDev.getInstance(PullRequestManager.class).get(requestId);
	}

	@Override
	public boolean isMinor() {
		return true;
	}
	
	@Override
	public ActivityDetail getActivityDetail() {
		return ActivityDetail.referencedFrom(getReferencedFrom());
	}
	
}
