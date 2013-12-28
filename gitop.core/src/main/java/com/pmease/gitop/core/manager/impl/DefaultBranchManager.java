package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Project;

@Singleton
public class DefaultBranchManager extends AbstractGenericDao<Branch> implements BranchManager {

	@Inject
	public DefaultBranchManager(GeneralDao generalDao) {
		super(generalDao);
	}

    @Transactional
    @Override
    public Branch findBy(Project project, String name) {
        return find(new Criterion[]{Restrictions.eq("project", project), Restrictions.eq("name", name)});
    }

	@Override
	public Branch findDefault(Project project) {
		// TODO Auto-generated method stub
		return null;
	}

}
