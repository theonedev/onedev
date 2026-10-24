package io.onedev.server.search.entity.workspace;

import java.io.Serial;

import io.onedev.server.model.Workspace;

public class PendingCriteria extends StatusCriteria {

	@Serial
	private static final long serialVersionUID = 1L;

	@Override
	public Workspace.Status getStatus() {
		return Workspace.Status.PENDING;
	}

	@Override
	public String toStringWithoutParens() {
		return WorkspaceQuery.getRuleName(WorkspaceQueryLexer.Pending);
	}

}
