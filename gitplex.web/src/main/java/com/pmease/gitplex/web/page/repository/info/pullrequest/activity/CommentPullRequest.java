package com.pmease.gitplex.web.page.repository.info.pullrequest.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.model.EntityModel;

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
		return new CommentActivityPanel(panelId, new EntityModel<PullRequestComment>(comment));
	}

	@Override
	public User getUser() {
		return comment.getUser();
	}

}
