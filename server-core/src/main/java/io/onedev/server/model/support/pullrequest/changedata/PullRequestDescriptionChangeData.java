package io.onedev.server.model.support.pullrequest.changedata;

import org.apache.wicket.Component;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.util.CommentAware;

public class PullRequestDescriptionChangeData implements PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final String oldDescription;
	
	private final String newDescription;
	
	public PullRequestDescriptionChangeData(String oldDescription, String newDescription) {
		this.oldDescription = oldDescription;
		this.newDescription = newDescription;
	}
	
	public String getOldDescription() {
		return oldDescription;
	}

	public String getNewDescription() {
		return newDescription;
	}

	@Override
	public String getActivity(PullRequest withRequest) {
		String activity = "changed description";
		if (withRequest != null)
			activity += " of pull request " + withRequest.getNumberAndTitle();
		return activity;
	}

	@Override
	public Component render(String componentId, PullRequestChange change) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public CommentAware getCommentAware() {
		return null;
	}

}
