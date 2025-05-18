package io.onedev.server.entitymanager;

import io.onedev.server.model.IssueDescriptionRevision;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueDescriptionRevisionManager extends EntityManager<IssueDescriptionRevision> {

    void create(IssueDescriptionRevision revision);
		
}
