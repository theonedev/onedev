package com.pmease.gitplex.web.page.repository.pullrequest.activity;

import com.pmease.gitplex.core.GitPlex;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.model.PullRequestComment;

@SuppressWarnings("serial")
public class PullRequestCommentModel extends LoadableDetachableModel<PullRequestComment> {

	private final Long commentId;
	
	public PullRequestCommentModel(Long commentId) {
		this.commentId = commentId;
	}
	
	@Override
	protected PullRequestComment load() {
		return GitPlex.getInstance(Dao.class).load(PullRequestComment.class, commentId);
	}

}
