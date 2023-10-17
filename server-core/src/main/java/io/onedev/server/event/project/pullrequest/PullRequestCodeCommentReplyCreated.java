package io.onedev.server.event.project.pullrequest;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.CodeCommentReplyManager;
import io.onedev.server.web.UrlManager;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.commenttext.CommentText;
import io.onedev.server.util.commenttext.MarkdownText;

public class PullRequestCodeCommentReplyCreated extends PullRequestCodeCommentEvent {

	private static final long serialVersionUID = 1L;
	
	private final Long replyId;
	
	public PullRequestCodeCommentReplyCreated(PullRequest request, CodeCommentReply reply) {
		super(reply.getUser(), reply.getDate(), request, reply.getComment());
		replyId = reply.getId();
	}

	public CodeCommentReply getReply() {
		return OneDev.getInstance(CodeCommentReplyManager.class).load(replyId);
	}

	@Override
	protected CommentText newCommentText() {
		return new MarkdownText(getProject(), getReply().getContent());
	}

	@Override
	public String getActivity() {
		return "replied code comment"; 
	}

	@Override
	public String getUrl() {
		return OneDev.getInstance(UrlManager.class).urlFor(getReply());
	}

}
