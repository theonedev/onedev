package com.pmease.gitplex.core.manager.impl;

import static com.pmease.commons.git.Change.Status.ADDED;
import static com.pmease.commons.git.Change.Status.DELETED;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.git.RevAwareChange;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestComment;

@Singleton
public class DefaultPullRequestCommentManager implements PullRequestCommentManager {

	private final Dao dao;
	
	@Inject
	public DefaultPullRequestCommentManager(Dao dao) {
		this.dao = dao;
	}

	@Override
	public List<PullRequestComment> findByChange(PullRequest request, RevAwareChange change) {
		Preconditions.checkArgument(GitUtils.isHash(change.getOldRevision()));
		Preconditions.checkArgument(GitUtils.isHash(change.getNewRevision()));
		EntityCriteria<PullRequestComment> criteria = EntityCriteria.of(PullRequestComment.class);
		criteria.add(Restrictions.eq("request", request));

		Conjunction oldConj = Restrictions.conjunction();
		oldConj.add(Restrictions.eq("inlineInfo.commit", change.getOldRevision()));
		oldConj.add(Restrictions.eq("inlineInfo.file", change.getOldPath()));
		
		Conjunction newConj = Restrictions.conjunction();
		newConj.add(Restrictions.eq("inlineInfo.commit", change.getNewRevision()));
		newConj.add(Restrictions.eq("inlineInfo.file", change.getNewPath()));
		
		if (change.getStatus() == ADDED) 
			criteria.add(oldConj);
		else if (change.getStatus() == DELETED) 
			criteria.add(newConj);
		else 
			criteria.add(Restrictions.or(oldConj, newConj));
		
		return dao.query(criteria);
	}

}
