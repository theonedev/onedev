package io.onedev.server.search.entity.workspace;

import io.onedev.server.model.Workspace;

public class ActiveCriteria extends StatusCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public Workspace.Status getStatus() {
		return Workspace.Status.ACTIVE;
	}

	@Override
	public String toStringWithoutParens() {
		return WorkspaceQuery.getRuleName(WorkspaceQueryLexer.Active);
	}

}
