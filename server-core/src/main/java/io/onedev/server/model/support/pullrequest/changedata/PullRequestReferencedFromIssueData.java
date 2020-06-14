package io.onedev.server.model.support.pullrequest.changedata;

import org.apache.wicket.Component;

import io.onedev.server.model.Issue;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.util.CommentAware;
import io.onedev.server.web.component.issue.referencedfrom.ReferencedFromIssuePanel;

public class PullRequestReferencedFromIssueData implements PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final Long issueId;
	
	public PullRequestReferencedFromIssueData(Issue issue) {
		this.issueId = issue.getId();
	}
	
	public Long getIssueId() {
		return issueId;
	}

	@Override
	public Component render(String componentId, PullRequestChange change) {
		return new ReferencedFromIssuePanel(componentId, issueId);
	}
	
	@Override
	public String getActivity(PullRequest withRequest) {
		if (withRequest != null)
			return "An issue referenced pull request " + withRequest.getNumberAndTitle();
		else
			return "Referenced from issue";
	}

	@Override
	public CommentAware getCommentAware() {
		return null;
	}


}
