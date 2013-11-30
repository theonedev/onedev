package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.commons.util.namedentity.EntityLoader;
import com.pmease.commons.util.namedentity.NamedEntity;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Project;

@Singleton
public class DefaultBranchManager extends AbstractGenericDao<Branch> implements BranchManager {

	@Inject
	public DefaultBranchManager(GeneralDao generalDao) {
		super(generalDao);
	}

	@Sessional
	@Override
	public Branch find(Project project, String name) {
		return find(project, name, false);
	}

    @Transactional
    @Override
    public Branch find(Project project, String name, boolean createIfNotExist) {
        Branch branch = find(new Criterion[]{Restrictions.eq("project", project), Restrictions.eq("name", name)});
        if (branch == null && createIfNotExist) {
            branch = new Branch();
            branch.setName(name);
            branch.setProject(project);
            save(branch);
        }
        return branch;
    }

	@Override
	public EntityLoader asEntityLoader(final Project project) {
		return new EntityLoader() {

			@Override
			public NamedEntity get(final Long id) {
				final Branch branch = DefaultBranchManager.this.get(id);
				if (branch != null) {
					return new NamedEntity() {

						@Override
						public Long getId() {
							return id;
						}

						@Override
						public String getName() {
							return branch.getName();
						}
						
					};
				} else {
					return null;
				}
			}

			@Override
			public NamedEntity get(String name) {
				final Branch branch = find(project, name);
				if (branch != null) {
					return new NamedEntity() {

						@Override
						public Long getId() {
							return branch.getId();
						}

						@Override
						public String getName() {
							return branch.getName();
						}
						
					};
				} else {
					return null;
				}
			}
			
		};
	}
    
}
