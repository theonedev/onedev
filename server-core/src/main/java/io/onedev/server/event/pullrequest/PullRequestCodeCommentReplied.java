package io.onedev.server.event.pullrequest;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.PullRequest;
import io.onedev.server.persistence.dao.Dao;

public class PullRequestCodeCommentReplied extends PullRequestCodeCommentEvent {

	private final CodeCommentReply reply;
	
	public PullRequestCodeCommentReplied(PullRequest request, CodeCommentReply reply) {
		super(reply.getUser(), reply.getDate(), request, reply.getComment());
		this.reply = reply;
	}

	public CodeCommentReply getReply() {
		return reply;
	}

	@Override
	public String getMarkdown() {
		return reply.getContent();
	}

	@Override
	public String getActivity() {
		return "replied code comment"; 
	}

	@Override
	public PullRequestEvent cloneIn(Dao dao) {
		return new PullRequestCodeCommentReplied(
				dao.load(PullRequest.class, getRequest().getId()), 
				dao.load(CodeCommentReply.class, reply.getId()));
	}

	@Override
	public String getUrl() {
		return OneDev.getInstance(UrlManager.class).urlFor(reply);
	}

}
