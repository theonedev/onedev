package io.onedev.server.entitymanager;

import io.onedev.server.model.PullRequestCommentRevision;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestCommentRevisionManager extends EntityManager<PullRequestCommentRevision> {

    void create(PullRequestCommentRevision revision);
    
}
