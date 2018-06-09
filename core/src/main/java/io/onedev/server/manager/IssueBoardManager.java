package io.onedev.server.manager;

import javax.annotation.Nullable;

import io.onedev.server.model.IssueBoard;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueBoardManager extends EntityManager<IssueBoard> {

	@Nullable
	IssueBoard find(Project project, String name);
	
}
