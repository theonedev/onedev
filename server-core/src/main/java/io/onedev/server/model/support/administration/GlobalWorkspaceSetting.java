package io.onedev.server.model.support.administration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import javax.validation.Valid;

import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.workspace.NamedWorkspaceQuery;

@Editable
public class GlobalWorkspaceSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<NamedWorkspaceQuery> namedQueries = new ArrayList<>();

	public GlobalWorkspaceSetting() {
		namedQueries.add(new NamedWorkspaceQuery("All", null));
		namedQueries.add(new NamedWorkspaceQuery("Queueing", "queueing"));
		namedQueries.add(new NamedWorkspaceQuery("Started", "started"));
		namedQueries.add(new NamedWorkspaceQuery("Stopped", "stopped"));
		namedQueries.add(new NamedWorkspaceQuery("Created by me", "created by me"));
	}

	@Valid
	public List<NamedWorkspaceQuery> getNamedQueries() {
		return namedQueries;
	}

	public void setNamedQueries(List<NamedWorkspaceQuery> namedQueries) {
		this.namedQueries = namedQueries;
	}

	@Nullable
	public NamedWorkspaceQuery getNamedQuery(String name) {
		for (NamedWorkspaceQuery namedQuery : getNamedQueries()) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}

}
