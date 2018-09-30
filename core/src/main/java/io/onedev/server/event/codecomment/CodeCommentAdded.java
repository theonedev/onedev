package io.onedev.server.event.codecomment;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.PullRequest;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(name="created")
public class CodeCommentAdded extends CodeCommentEvent {

	public CodeCommentAdded(CodeComment comment, PullRequest request) {
		super(comment.getUser(), comment.getDate(), comment, request);
	}

	@Override
	public String getMarkdown() {
		return getComment().getContent();
	}

}
