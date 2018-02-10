package com.turbodev.server.event.codecomment;

import java.util.Date;

import com.turbodev.server.model.CodeComment;
import com.turbodev.server.model.PullRequest;
import com.turbodev.server.model.User;
import com.turbodev.server.util.editable.annotation.Editable;

@Editable(name="created")
public class CodeCommentCreated extends CodeCommentEvent {

	public CodeCommentCreated(CodeComment comment, PullRequest request) {
		super(comment, request);
	}

	@Override
	public String getMarkdown() {
		return getComment().getContent();
	}

	@Override
	public User getUser() {
		return getComment().getUser();
	}

	@Override
	public Date getDate() {
		return getComment().getDate();
	}

}
