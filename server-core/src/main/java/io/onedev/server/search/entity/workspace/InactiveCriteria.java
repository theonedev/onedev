package io.onedev.server.search.entity.workspace;

import java.io.Serial;

import io.onedev.server.model.Workspace;

public class InactiveCriteria extends StatusCriteria {

	@Serial
	private static final long serialVersionUID = 1L;

	@Override
	public Workspace.Status getStatus() {
		return Workspace.Status.INACTIVE;
	}

	@Override
	public String toStringWithoutParens() {
		return WorkspaceQuery.getRuleName(WorkspaceQueryLexer.Inactive);
	}

}
