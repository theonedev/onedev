package io.onedev.server.model.support.administration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.support.build.NamedBuildQuery;

public class GlobalBuildSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<NamedBuildQuery> namedQueries = new ArrayList<>();
	
	private List<String> listParams = new ArrayList<>();
	
	public GlobalBuildSetting() {
		namedQueries.add(new NamedBuildQuery("All", null));
		namedQueries.add(new NamedBuildQuery("Successful", "successful"));
		namedQueries.add(new NamedBuildQuery("Failed", "failed"));
		namedQueries.add(new NamedBuildQuery("Cancelled", "cancelled"));
		namedQueries.add(new NamedBuildQuery("Timed out", "timed out"));
		namedQueries.add(new NamedBuildQuery("Running", "running"));
		namedQueries.add(new NamedBuildQuery("Waiting", "waiting"));
		namedQueries.add(new NamedBuildQuery("Pending", "pending"));
		namedQueries.add(new NamedBuildQuery("Build recently", "\"Submit Date\" is since \"last week\""));
	}
	
	public List<NamedBuildQuery> getNamedQueries() {
		return namedQueries;
	}

	public void setNamedQueries(List<NamedBuildQuery> namedQueries) {
		this.namedQueries = namedQueries;
	}
	
	public List<String> getListParams() {
		return listParams;
	}

	public void setListParams(List<String> listParams) {
		this.listParams = listParams;
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
