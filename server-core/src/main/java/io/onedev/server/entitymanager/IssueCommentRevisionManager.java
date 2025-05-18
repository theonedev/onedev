package io.onedev.server.entitymanager;

import io.onedev.server.model.IssueCommentRevision;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueCommentRevisionManager extends EntityManager<IssueCommentRevision> {

    void create(IssueCommentRevision revision);
    
}
