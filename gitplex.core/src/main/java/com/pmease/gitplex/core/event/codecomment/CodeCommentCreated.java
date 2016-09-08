package com.pmease.gitplex.core.event.codecomment;

import javax.annotation.Nullable;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.PullRequest;

@Editable(name="created")
public class CodeCommentCreated extends CodeCommentEvent {

	public CodeCommentCreated(CodeComment comment, @Nullable PullRequest request) {
		super(comment, comment.getUser(), comment.getDate(), request);
	}

	@Override
	public String getMarkdown() {
		return getComment().getContent();
	}

}
