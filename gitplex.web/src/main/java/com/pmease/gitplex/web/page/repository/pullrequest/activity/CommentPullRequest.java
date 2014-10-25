package com.pmease.gitplex.web.page.repository.pullrequest.activity;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.PullRequestComment;

@SuppressWarnings("serial")
public class CommentPullRequest extends AbstractCommentPullRequest {

	private final Long commentId;
	
	public CommentPullRequest(PullRequestComment comment) {
		this.commentId = comment.getId();
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

}
