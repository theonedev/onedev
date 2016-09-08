package com.pmease.gitplex.core.event.pullrequest;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.event.MarkdownAware;

@Editable(name="commented code")
public class PullRequestCodeCommented extends PullRequestChangeEvent implements MarkdownAware {

	private final CodeComment comment;
	
	public PullRequestCodeCommented(PullRequest request, CodeComment comment) {
		super(request, comment.getUser(), comment.getDate());
		this.comment = comment;
	}

	public CodeComment getComment() {
		return comment;
	}

	@Override
	public String getMarkdown() {
		return getComment().getContent();
	}

}
