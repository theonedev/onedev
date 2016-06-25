package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;

@SuppressWarnings("serial")
public class CommentPullRequest extends AbstractRenderableActivity {

	private final Long commentId;
	
	@Override
	public Date getDate() {
		return getComment().getDate();
	}

	@Override
	public Account getUser() {
		return getComment().getUser();
	}

	public CommentPullRequest(PullRequestComment comment) {
		super(comment.getRequest(), comment.getUser(), comment.getDate());
		this.commentId = comment.getId();
	}
	
	@Override
	public Panel render(String panelId) {
		return new CommentActivityPanel(panelId, this);
	}

	public PullRequestComment getComment() {
		return GitPlex.getInstance(PullRequestCommentManager.class).load(commentId);
	}

}
