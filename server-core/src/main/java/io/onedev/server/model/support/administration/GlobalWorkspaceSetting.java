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
		namedQueries.add(new NamedWorkspaceQuery("Pending", "pending"));
		namedQueries.add(new NamedWorkspaceQuery("Active", "active"));
		namedQueries.add(new NamedWorkspaceQuery("Inactive", "inactive"));
		namedQueries.add(new NamedWorkspaceQuery("Created by me", "created by me"));
		namedQueries.add(new NamedWorkspaceQuery("Created recently", "\"Create Date\" is since \"last week\""));		
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
