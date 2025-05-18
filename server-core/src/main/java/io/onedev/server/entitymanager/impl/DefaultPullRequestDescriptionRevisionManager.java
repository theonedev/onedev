package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.onedev.server.entitymanager.PullRequestDescriptionRevisionManager;
import io.onedev.server.model.PullRequestDescriptionRevision;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultPullRequestDescriptionRevisionManager extends BaseEntityManager<PullRequestDescriptionRevision> 
        implements PullRequestDescriptionRevisionManager {

    @Inject
    public DefaultPullRequestDescriptionRevisionManager(Dao dao) {
        super(dao);
    }

    @Transactional
    @Override
    public void create(PullRequestDescriptionRevision revision) {
        Preconditions.checkArgument(revision.isNew());
        dao.persist(revision);
    }
    
} 