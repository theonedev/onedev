package io.onedev.server.service.impl;

import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.onedev.server.model.PullRequestCommentRevision;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.PullRequestCommentRevisionService;

@Singleton
public class DefaultPullRequestCommentRevisionService extends BaseEntityService<PullRequestCommentRevision>
        implements PullRequestCommentRevisionService {

    @Transactional
    @Override
    public void create(PullRequestCommentRevision revision) {
        Preconditions.checkArgument(revision.isNew());
        dao.persist(revision);
    }
    
} 