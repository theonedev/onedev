package com.pmease.gitop.web.page.repository.pullrequest.activity;

import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.PullRequestComment;

@SuppressWarnings("serial")
public class PullRequestCommentModel extends LoadableDetachableModel<PullRequestComment> {

	private final Long commentId;
	
	public PullRequestCommentModel(Long commentId) {
		this.commentId = commentId;
	}
	
	@Override
	protected PullRequestComment load() {
		return Gitop.getInstance(Dao.class).load(PullRequestComment.class, commentId);
	}

}
