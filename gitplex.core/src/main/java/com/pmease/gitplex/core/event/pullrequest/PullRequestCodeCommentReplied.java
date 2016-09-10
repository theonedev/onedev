package com.pmease.gitplex.core.event.pullrequest;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.CodeCommentReply;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.event.MarkdownAware;

@Editable(name="replied code comment")
public class PullRequestCodeCommentReplied extends PullRequestCodeCommentEvent implements MarkdownAware {

	private final CodeCommentReply reply;
	
	public PullRequestCodeCommentReplied(PullRequest request, CodeCommentReply reply) {
		super(request, reply.getUser(), reply.getDate(), reply.getComment());
		this.reply = reply;
	}

	public CodeCommentReply getReply() {
		return reply;
	}

	@Override
	public String getMarkdown() {
		return getReply().getContent();
	}

}
