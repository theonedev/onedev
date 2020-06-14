package io.onedev.server.model.support.pullrequest.changedata;

import org.apache.wicket.Component;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.util.CommentAware;
import io.onedev.server.web.component.pullrequest.referencedfrom.ReferencedFromPullRequestPanel;

public class PullRequestReferencedFromPullRequestData implements PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final Long requestId;
	
	public PullRequestReferencedFromPullRequestData(PullRequest request) {
		this.requestId = request.getId();
	}
	
	public Long getRequestId() {
		return requestId;
	}

	@Override
	public Component render(String componentId, PullRequestChange change) {
		return new ReferencedFromPullRequestPanel(componentId, requestId);
	}
	
	@Override
	public String getActivity(PullRequest withRequest) {
		if (withRequest != null)
			return "Another pull request referenced pull request " + withRequest.getNumberAndTitle();
		else
			return "Referenced from other pull request";
	}

	@Override
	public CommentAware getCommentAware() {
		return null;
	}

}
