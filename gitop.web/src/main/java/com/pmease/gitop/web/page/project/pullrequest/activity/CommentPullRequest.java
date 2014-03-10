package com.pmease.gitop.web.page.project.pullrequest.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitop.model.PullRequestComment;

public class CommentPullRequest implements PullRequestActivity {

	private final PullRequestComment comment;

	public CommentPullRequest(PullRequestComment comment) {
		this.comment = comment;
	}
	
	@Override
	public Date getDate() {
		return comment.getDate();
	}

	@Override
	public Panel render(String panelId) {
		return new CommentActivityPanel(panelId, new PullRequestCommentModel(comment.getId()));
	}
	
}
