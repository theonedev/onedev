package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.MergeRequestManager;
import com.pmease.gitop.core.model.Branch;
import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.User;

@Singleton
public class DefaultMergeRequestManager extends AbstractGenericDao<MergeRequest> implements
		MergeRequestManager {

	@Inject
	public DefaultMergeRequestManager(GeneralDao generalDao) {
		super(generalDao);
	}

	@Sessional
	@Override
	public MergeRequest findOpened(Branch target, Branch source, User submitter) {
		Criterion statusCriterion =
				Restrictions.or(
						Restrictions.eq("status", MergeRequest.Status.PENDING_APPROVAL),
						Restrictions.eq("status", MergeRequest.Status.PENDING_UPDATE),
						Restrictions.eq("status", MergeRequest.Status.PENDING_MERGE));

		return find(Restrictions.eq("target", target), Restrictions.eq("source", source),
				Restrictions.eq("submitter", submitter), statusCriterion);
	}

}
