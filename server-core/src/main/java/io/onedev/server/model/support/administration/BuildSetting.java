package io.onedev.server.model.support.administration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.support.NamedBuildQuery;

public class BuildSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<NamedBuildQuery> namedQueries = new ArrayList<>();
	
	public BuildSetting() {
		namedQueries.add(new NamedBuildQuery("All", "all"));
		namedQueries.add(new NamedBuildQuery("Successful", "successful"));
		namedQueries.add(new NamedBuildQuery("Failed", "failed"));
		namedQueries.add(new NamedBuildQuery("Failed eventually", "failed and not(will retry)"));
		namedQueries.add(new NamedBuildQuery("In error", "in error"));
		namedQueries.add(new NamedBuildQuery("Cancelled", "cancelled"));
		namedQueries.add(new NamedBuildQuery("Timed out", "timed out"));
		namedQueries.add(new NamedBuildQuery("Running", "running"));
		namedQueries.add(new NamedBuildQuery("Waiting", "waiting"));
		namedQueries.add(new NamedBuildQuery("Queueing", "queueing"));
		namedQueries.add(new NamedBuildQuery("Build recently", "\"Submit Date\" is after \"last week\""));
	}
	
	public List<NamedBuildQuery> getNamedQueries() {
		return namedQueries;
	}

	public void setNamedQueries(List<NamedBuildQuery> namedQueries) {
		this.namedQueries = namedQueries;
	}
	
	@Nullable
	public NamedBuildQuery getNamedQuery(String name) {
		for (NamedBuildQuery namedQuery: getNamedQueries()) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}
	
}
