package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.core.manager.PullRequestUpdateManager;
import com.pmease.gitop.core.model.Branch;
import com.pmease.gitop.core.model.PullRequest;
import com.pmease.gitop.core.model.PullRequestUpdate;
import com.pmease.gitop.core.model.User;

@Singleton
public class DefaultPullRequestManager extends AbstractGenericDao<PullRequest> implements
		PullRequestManager {

	private final PullRequestUpdateManager pullRequestUpdateManager;

	@Inject
	public DefaultPullRequestManager(GeneralDao generalDao,
			PullRequestUpdateManager pullRequestUpdateManager) {
		super(generalDao);
		this.pullRequestUpdateManager = pullRequestUpdateManager;
	}

	@Sessional
	@Override
	public PullRequest findOpened(Branch target, Branch source, User submitter) {
		Criterion statusCriterion =
				Restrictions.or(
						Restrictions.eq("status", PullRequest.Status.PENDING_CHECK),
						Restrictions.eq("status", PullRequest.Status.PENDING_APPROVAL),
						Restrictions.eq("status", PullRequest.Status.PENDING_UPDATE),
						Restrictions.eq("status", PullRequest.Status.PENDING_MERGE));

		return find(Restrictions.eq("target", target), Restrictions.eqOrIsNull("source", source),
				Restrictions.eq("submitter", submitter), statusCriterion);
	}

	@Transactional
	@Override
	public void delete(PullRequest request) {
		for (PullRequestUpdate update : request.getUpdates())
			pullRequestUpdateManager.delete(update);
		super.delete(request);
	}

}
