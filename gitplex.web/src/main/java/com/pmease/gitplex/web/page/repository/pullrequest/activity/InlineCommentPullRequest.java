package com.pmease.gitplex.web.page.repository.pullrequest.activity;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.PullRequestInlineComment;

@SuppressWarnings("serial")
public class InlineCommentPullRequest extends AbstractCommentPullRequest {

	private final Long commentId;
	
	public InlineCommentPullRequest(PullRequestInlineComment comment) {
		this.commentId = comment.getId();
	}
	
	@Override
	public Panel render(String panelId) {
		return new InlineCommentActivityPanel(panelId, new LoadableDetachableModel<PullRequestInlineComment>(){

			@Override
			protected PullRequestInlineComment load() {
				return getComment();
			}
			
		});
	}

	public PullRequestInlineComment getComment() {
		return GitPlex.getInstance(Dao.class).load(PullRequestInlineComment.class, commentId);
	}

}
