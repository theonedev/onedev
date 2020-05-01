package io.onedev.server.model.support.administration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.support.NamedProjectQuery;

public class GlobalProjectSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<NamedProjectQuery> namedQueries = new ArrayList<>();
	
	public GlobalProjectSetting() {
		namedQueries.add(new NamedProjectQuery("All projects", null));
		namedQueries.add(new NamedProjectQuery("My projects", "owned by me"));
	}
	
	public List<NamedProjectQuery> getNamedQueries() {
		return namedQueries;
	}

	public void setNamedQueries(List<NamedProjectQuery> namedQueries) {
		this.namedQueries = namedQueries;
	}
	
	@Nullable
	public NamedProjectQuery getNamedQuery(String name) {
		for (NamedProjectQuery namedQuery: getNamedQueries()) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}
	
}
