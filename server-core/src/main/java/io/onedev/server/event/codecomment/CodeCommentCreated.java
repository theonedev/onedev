package io.onedev.server.event.codecomment;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.PullRequest;

public class CodeCommentCreated extends CodeCommentEvent implements MarkdownAware {

	public CodeCommentCreated(CodeComment comment, PullRequest request) {
		super(comment.getUser(), comment.getCreateDate(), comment, request);
	}

	@Override
	public String getMarkdown() {
		return getComment().getContent();
	}

	@Override
	public String getActivity(boolean withEntity) {
		String activity = "commented";
		if (withEntity)
			activity += " on file " + getComment().getMarkPos().getPath();
		return activity;
	}

}
