package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.activity;

import java.util.Date;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;

@SuppressWarnings("serial")
public class CommentedRenderer extends AbstractRenderer {

	private final Long commentId;
	
	@Override
	public Date getDate() {
		return getComment().getDate();
	}

	@Override
	public Account getUser() {
		return getComment().getUser();
	}

	public CommentedRenderer(PullRequestComment comment) {
		super(comment.getRequest(), comment.getUser(), comment.getDate());
		this.commentId = comment.getId();
	}
	
	@Override
	public ActivityPanel render(String panelId) {
		return new CommentedPanel(panelId, this);
	}

	public PullRequestComment getComment() {
		return GitPlex.getInstance(PullRequestCommentManager.class).load(commentId);
	}

}
