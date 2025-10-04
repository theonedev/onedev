package io.onedev.server.service;

import io.onedev.server.model.IssueCommentRevision;

public interface IssueCommentRevisionService extends EntityService<IssueCommentRevision> {

    void create(IssueCommentRevision revision);
    
}
