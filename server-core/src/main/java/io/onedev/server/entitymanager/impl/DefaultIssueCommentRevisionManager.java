package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.onedev.server.entitymanager.IssueCommentRevisionManager;
import io.onedev.server.model.IssueCommentRevision;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultIssueCommentRevisionManager extends BaseEntityManager<IssueCommentRevision> 
        implements IssueCommentRevisionManager {

    @Inject
    public DefaultIssueCommentRevisionManager(Dao dao) {
        super(dao);
    }

    @Transactional
    @Override
    public void create(IssueCommentRevision revision) {
        Preconditions.checkArgument(revision.isNew());
        dao.persist(revision);
    }
} 