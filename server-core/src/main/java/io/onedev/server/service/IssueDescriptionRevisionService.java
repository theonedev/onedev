package io.onedev.server.service;

import io.onedev.server.model.IssueDescriptionRevision;

public interface IssueDescriptionRevisionService extends EntityService<IssueDescriptionRevision> {

    void create(IssueDescriptionRevision revision);
		
}
