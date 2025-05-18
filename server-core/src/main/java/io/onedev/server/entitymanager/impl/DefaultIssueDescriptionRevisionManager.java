package io.onedev.server.entitymanager.impl;

import io.onedev.server.entitymanager.IssueDescriptionRevisionManager;
import io.onedev.server.model.IssueDescriptionRevision;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;

@Singleton
public class DefaultIssueDescriptionRevisionManager extends BaseEntityManager<IssueDescriptionRevision> 
        implements IssueDescriptionRevisionManager {

    @Inject
    public DefaultIssueDescriptionRevisionManager(Dao dao) {
        super(dao);
    }

    @Transactional
    @Override
    public void create(IssueDescriptionRevision revision) {
        Preconditions.checkArgument(revision.isNew());
        dao.persist(revision);
    }
    
} 