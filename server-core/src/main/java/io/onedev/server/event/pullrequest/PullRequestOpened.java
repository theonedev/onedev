package io.onedev.server.event.pullrequest;

import io.onedev.server.model.PullRequest;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.commenttext.CommentText;
import io.onedev.server.util.commenttext.MarkdownText;

public class PullRequestOpened extends PullRequestEvent {

	public PullRequestOpened(PullRequest request) {
		super(request.getSubmitter(), request.getSubmitDate(), request);
	}

	@Override
	protected CommentText newCommentText() {
		return getRequest().getDescription()!=null? new MarkdownText(getProject(), getRequest().getDescription()): null;
	}

	@Override
	public String getActivity() {
		return "opened";
	}

	@Override
	public PullRequestEvent cloneIn(Dao dao) {
		return new PullRequestOpened(dao.load(PullRequest.class, getRequest().getId()));
	}

}
