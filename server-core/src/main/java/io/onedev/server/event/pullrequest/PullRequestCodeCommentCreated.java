package io.onedev.server.event.pullrequest;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.PullRequest;
import io.onedev.server.persistence.dao.Dao;

public class PullRequestCodeCommentCreated extends PullRequestCodeCommentEvent {

	public PullRequestCodeCommentCreated(PullRequest request, CodeComment comment) {
		super(comment.getUser(), comment.getCreateDate(), request, comment);
	}

	@Override
	public String getMarkdown() {
		return getComment().getContent();
	}

	@Override
	public String getActivity() {
		return "created code comment"; 
	}

	@Override
	public PullRequestEvent cloneIn(Dao dao) {
		return new PullRequestCodeCommentCreated(
				dao.load(PullRequest.class, getRequest().getId()), 
				dao.load(CodeComment.class, getComment().getId()));
	}

	@Override
	public String getUrl() {
		return OneDev.getInstance(UrlManager.class).urlFor(getComment());
	}

}
