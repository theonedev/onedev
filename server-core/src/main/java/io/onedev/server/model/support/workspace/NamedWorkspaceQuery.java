package io.onedev.server.model.support.workspace;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.annotation.WorkspaceQuery;
import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.NamedQuery;

@Editable
public class NamedWorkspaceQuery implements NamedQuery {

	private static final long serialVersionUID = 1L;

	private String name;

	private String query;

	public NamedWorkspaceQuery(String name, String query) {
		this.name = name;
		this.query = query;
	}

	public NamedWorkspaceQuery() {
	}

	@Editable
	@NotEmpty
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(placeholder="All")
	@WorkspaceQuery(withCurrentUserCriteria=true)
	@Override
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

}
