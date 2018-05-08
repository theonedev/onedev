package io.onedev.server.manager;

import javax.annotation.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueRelation;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueRelationManager extends EntityManager<IssueRelation> {
	
	@Nullable
	IssueRelation find(Issue current, Issue other);
	
}
