package com.pmease.gitplex.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.ObjectId;

import com.pmease.commons.hibernate.dao.AbstractEntityDao;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.CodeCommentRelation;
import com.pmease.gitplex.core.listener.CodeCommentListener;
import com.pmease.gitplex.core.manager.CodeCommentRelationManager;
import com.pmease.gitplex.core.manager.PullRequestInfoManager;

@Singleton
public class DefaultCodeCommentRelationManager extends AbstractEntityDao<CodeCommentRelation> 
		implements CodeCommentRelationManager, CodeCommentListener {

	private final PullRequestInfoManager pullRequestInfoManager;
	
	@Inject
	public DefaultCodeCommentRelationManager(Dao dao, PullRequestInfoManager pullRequestInfoManager) {
		super(dao);
		this.pullRequestInfoManager = pullRequestInfoManager;
	}

	@Override
	public void onSaveComment(CodeComment comment) {
		ObjectId commitId = ObjectId.fromString(comment.getCommit());
		for (String requestUUId: pullRequestInfoManager.getRequests(comment.getDepot(), commitId)) {
			
		}
	}

	@Override
	public void onDeleteComment(CodeComment comment) {
	}

}
