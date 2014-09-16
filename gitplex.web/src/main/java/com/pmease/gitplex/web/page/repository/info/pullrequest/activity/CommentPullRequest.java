package com.pmease.gitplex.web.page.repository.info.pullrequest.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
public class CommentPullRequest implements PullRequestActivity {

	private final Long commentId;

	public CommentPullRequest(PullRequestComment comment) {
		this.commentId = comment.getId();
	}
	
	@Override
	public Date getDate() {
		return getComment().getDate();
	}

	@Override
	public Panel render(String panelId) {
		return new CommentActivityPanel(panelId, new LoadableDetachableModel<PullRequestComment>(){

			@Override
			protected PullRequestComment load() {
				return getComment();
			}
			
		});
	}

	public PullRequestComment getComment() {
		return GitPlex.getInstance(Dao.class).load(PullRequestComment.class, commentId);
	}

	@Override
	public User getUser() {
		return getComment().getUser();
	}

}
