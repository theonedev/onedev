package com.pmease.gitplex.core.event.pullrequest;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.CodeCommentStatusChange;
import com.pmease.gitplex.core.entity.PullRequest;

@Editable(name="resolved code comment")
public class PullRequestCodeCommentResolved extends PullRequestChangeEvent implements MarkdownAware {

	private final CodeCommentStatusChange statusChange;
	
	public PullRequestCodeCommentResolved(PullRequest request, CodeCommentStatusChange statusChange) {
		super(request, statusChange.getUser(), statusChange.getDate());
		this.statusChange = statusChange;
	}

	public CodeCommentStatusChange getStatusChange() {
		return statusChange;
	}

	@Override
	public String getMarkdown() {
		return getStatusChange().getNote();
	}

}
