package io.onedev.server.service.impl;

import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.onedev.server.model.IssueCommentRevision;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.IssueCommentRevisionService;

@Singleton
public class DefaultIssueCommentRevisionService extends BaseEntityService<IssueCommentRevision>
        implements IssueCommentRevisionService {

    @Transactional
    @Override
    public void create(IssueCommentRevision revision) {
        Preconditions.checkArgument(revision.isNew());
        dao.persist(revision);
    }
} 