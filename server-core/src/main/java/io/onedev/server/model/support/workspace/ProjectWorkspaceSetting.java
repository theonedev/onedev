package io.onedev.server.model.support.workspace;

import java.io.Serializable;
import java.util.List;

import org.jspecify.annotations.Nullable;
import javax.validation.Valid;

import io.onedev.server.annotation.Editable;

@Editable
public class ProjectWorkspaceSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<NamedWorkspaceQuery> namedQueries;

	@Nullable
	@Valid
	public List<NamedWorkspaceQuery> getNamedQueries() {
		return namedQueries;
	}

	public void setNamedQueries(@Nullable List<NamedWorkspaceQuery> namedQueries) {
		this.namedQueries = namedQueries;
	}

}
