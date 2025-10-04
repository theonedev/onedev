package io.onedev.server.service;

import io.onedev.server.model.PullRequestCommentRevision;

public interface PullRequestCommentRevisionService extends EntityService<PullRequestCommentRevision> {

    void create(PullRequestCommentRevision revision);
    
}
