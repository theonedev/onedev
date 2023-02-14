package io.onedev.server.model.support.pullrequest.changedata;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entityreference.ReferencedFromAware;
import io.onedev.server.model.Issue;
import io.onedev.server.notification.ActivityDetail;

public class PullRequestReferencedFromIssueData 
		extends PullRequestChangeData implements ReferencedFromAware<Issue> {

	private static final long serialVersionUID = 1L;

	private final Long issueId;
	
	public PullRequestReferencedFromIssueData(Issue issue) {
		this.issueId = issue.getId();
	}
	
	public Long getIssueId() {
		return issueId;
	}

	@Override
	public boolean isMinor() {
		return true;
	}

	@Override
	public String getActivity() {
		return "referenced from issue";
	}

	@Override
	public Issue getReferencedFrom() {
		return OneDev.getInstance(IssueManager.class).get(issueId);
	}

	@Override
	public ActivityDetail getActivityDetail() {
		return ActivityDetail.referencedFrom(getReferencedFrom());
	}
	
}
