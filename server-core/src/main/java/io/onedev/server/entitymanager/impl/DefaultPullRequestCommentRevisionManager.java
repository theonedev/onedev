package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.onedev.server.entitymanager.PullRequestCommentRevisionManager;
import io.onedev.server.model.PullRequestCommentRevision;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultPullRequestCommentRevisionManager extends BaseEntityManager<PullRequestCommentRevision> 
        implements PullRequestCommentRevisionManager {

    @Inject
    public DefaultPullRequestCommentRevisionManager(Dao dao) {
        super(dao);
    }

    @Transactional
    @Override
    public void create(PullRequestCommentRevision revision) {
        Preconditions.checkArgument(revision.isNew());
        dao.persist(revision);
    }
    
} 