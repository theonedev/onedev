package io.onedev.server.model.support.pullrequest.changedata;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entityreference.ReferencedFromAware;
import io.onedev.server.model.PullRequest;
import io.onedev.server.notification.ActivityDetail;

public class PullRequestReferencedFromPullRequestData 
		extends PullRequestChangeData implements ReferencedFromAware<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final Long requestId;
	
	public PullRequestReferencedFromPullRequestData(PullRequest request) {
		this.requestId = request.getId();
	}
	
	public Long getRequestId() {
		return requestId;
	}

	@Override
	public boolean isMinor() {
		return true;
	}
	
	@Override
	public String getActivity() {
		return "referenced from other pull request";
	}

	@Override
	public PullRequest getReferencedFrom() {
		return OneDev.getInstance(PullRequestManager.class).get(requestId);
	}

	@Override
	public ActivityDetail getActivityDetail() {
		return ActivityDetail.referencedFrom(getReferencedFrom());
	}
	
}
